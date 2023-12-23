package com.github.danielm94;

import com.github.danielm94.config.ConnectionPoolConfiguration;
import com.github.danielm94.credentials.ConnectionCredentials;
import lombok.NonNull;
import lombok.extern.flogger.Flogger;
import lombok.val;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("LombokGetterMayBeUsed")
@Flogger
public class ConnectionPoolManager {
    private static volatile ConnectionPoolManager instance;
    private final AtomicInteger poolCapacity;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger lowLoadCount = new AtomicInteger(0);
    private final ConnectionCredentials credentials;
    private BlockingQueue<Connection> connectionPool;
    private final ScheduledExecutorService leakDetectionService;
    private final ConnectionLeakDetector leakDetector;
    private final Set<Connection> activeConnectionSet;
    private boolean leakDetectionScheduled = false;
    private final ConnectionPoolConfiguration config;


    /**
     * Private constructor for ConnectionPoolManager.
     * Initializes the connection pool and other configurations.
     *
     * @param credentials The credentials used for creating database connections.
     * @throws SQLException If a database access error occurs.
     */
    private ConnectionPoolManager(ConnectionPoolConfiguration config, ConnectionCredentials credentials) throws SQLException {
        this.config = config;
        this.poolCapacity = new AtomicInteger(config.getInitialMaxPoolSize());
        this.connectionPool = new ArrayBlockingQueue<>(poolCapacity.get());
        this.credentials = credentials;
        this.leakDetectionService = Executors.newSingleThreadScheduledExecutor();
        this.leakDetector = new ConnectionLeakDetector(config.getConnectionLeakThreshold());
        this.activeConnectionSet = ConcurrentHashMap.newKeySet();
        fillConnectionPool(config.getInitialPoolSize());
    }


    /**
     * Retrieves the singleton instance of ConnectionPoolManager.
     * Throws an exception if the instance is not initialized yet.
     *
     * @return The singleton instance of ConnectionPoolManager.
     * @throws IllegalStateException If the instance is not initialized.
     */
    public static synchronized ConnectionPoolManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionPoolManager is not initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Initializes the singleton instance of the ConnectionPoolManager.
     * This method should be called once during the application startup.
     *
     * @param credentials The credentials used for creating database connections.
     * @throws SQLException If a database access error occurs.
     */
    public static synchronized void initialize(@NonNull ConnectionPoolConfiguration config, @NonNull ConnectionCredentials credentials) throws SQLException {
        if (instance == null) {
            instance = new ConnectionPoolManager(config, credentials);
        }
    }

    /**
     * Acquires a connection from the pool. If the pool is under high load, it attempts to expand the pool.
     * Validates and potentially refreshes the connection before returning it.
     *
     * @return A valid database connection.
     * @throws InterruptedException If the thread is interrupted while waiting for a connection.
     * @throws SQLException         If a database access error occurs or the connection is not valid.
     */
    public Connection getConnection() throws InterruptedException, SQLException {
        val highLoadThreshold = config.getHighLoadThreshold();
        val poolCapacityValue = poolCapacity.get();
        if (isPoolUnderHighLoad(poolCapacityValue, highLoadThreshold)) {
            handleHighLoad();
        }
        if (connectionPool.isEmpty()) {
            topUpConnections();
        }
        val timeout = config.getConnectionFromPoolTimeout();
        var connection = connectionPool.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (connection == null) return null;
        connection = validateConnection(connection);
        leakDetector.registerConnection(connection);
        scheduleLeakDetection();
        activeConnections.incrementAndGet();
        activeConnectionSet.add(connection);
        return connection;
    }

    /**
     * Returns a connection to the pool and checks for low load to potentially shrink the pool.
     * Validates the connection before returning it to the pool.
     *
     * @param connection The database connection to return.
     * @throws SQLException         If a database access error occurs.
     * @throws InterruptedException If the thread is interrupted while handling the connection return.
     * @throws NullPointerException If the connection object is null.
     */
    public void returnConnection(@NonNull Connection connection) throws SQLException, InterruptedException {
        activeConnectionSet.remove(connection);
        connection = validateConnection(connection);

        if (!connectionPool.offer(connection)) {
            log.atFine().log("Connection pool is full. Closing returned connection.");
            connection.close();
        }
        activeConnections.decrementAndGet();
        handleLowLoad();
    }

    /**
     * Handles connections that are detected as leaky by closing and removing them from the active set.
     *
     * @param connection The leaky connection to handle.
     * @throws SQLException         If a database access error occurs during connection closure.
     * @throws NullPointerException If the connection object is null.
     */
    public void handleLeakyConnections(@NonNull Connection connection) throws SQLException {
        activeConnectionSet.remove(connection);
        connection.close();
        activeConnections.decrementAndGet();

    }

    /**
     * Returns the number of free (available) connections in the pool.
     *
     * @return The number of available connections in the pool.
     */
    public int getFreeConnections() {
        return connectionPool.size();
    }

    /**
     * Returns the number of active (in-use) connections.
     *
     * @return The number of active connections.
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * Returns the free connection capacity that the pool can hold before resizing.
     *
     * @return The free connection capacity of the pool.
     */
    public int getPoolCapacity() {
        return poolCapacity.get();
    }

    /**
     * Checks whether the leak detection service is scheduled.
     *
     * @return True if it's scheduled, else false.
     */
    public boolean isLeakDetectionServiceScheduled() {
        return leakDetectionScheduled;
    }

    /**
     * Checks if a connection has been handed out by the connection pool manager and is expected to return.
     *
     * @param connection The connection to check (not null)
     * @return True if the connection is considered active and should be returned, else false.
     * @throws NullPointerException If the connection object is null.
     */
    public boolean isConnectionActive(@NonNull Connection connection) {
        return activeConnectionSet.contains(connection);
    }

    /**
     * Handles low load scenarios by potentially shrinking the pool.
     *
     * @throws SQLException         If a database access error occurs during pool resizing.
     * @throws InterruptedException If the thread is interrupted while shrinking the pool.
     */
    private synchronized void handleLowLoad() throws SQLException, InterruptedException {
        val poolCapacityValue = poolCapacity.get();
        val initialPoolSize = config.getInitialMaxPoolSize();

        val poolSizeIsAtLowestPossibleSize = poolCapacityValue == initialPoolSize;
        if (poolSizeIsAtLowestPossibleSize) return;

        val activeConnectionsValue = activeConnections.get();
        val loadFactor = (double) activeConnectionsValue / poolCapacityValue;
        val lowLoadThreshold = config.getLowLoadThreshold();

        val isNotUnderLowLoad = loadFactor >= lowLoadThreshold;
        if (isNotUnderLowLoad) {
            lowLoadCount.set(0);
            return;
        }

        if (lowLoadCount.incrementAndGet() >= config.getLowLoadHysteresisCount()) {
            val poolShrinkFactor = config.getLowLoadPoolShrinkFactor();
            val minimumPoolSize = Math.max(initialPoolSize, activeConnectionsValue);
            val newPoolSize = Math.min(minimumPoolSize, (int) (poolCapacityValue * poolShrinkFactor));
            shrinkPool(newPoolSize);
            lowLoadCount.set(0);
        }

    }

    /**
     * Shrinks the connection pool to a new, smaller capacity.
     * Closes connections that are removed from the pool.
     *
     * @param newPoolCapacity The new capacity for the pool.
     * @throws InterruptedException If the thread is interrupted while shrinking the pool.
     * @throws SQLException         If a database access error occurs during connection closure.
     */
    private void shrinkPool(int newPoolCapacity) throws InterruptedException, SQLException {
        poolCapacity.set(newPoolCapacity);
        val newPool = new ArrayBlockingQueue<Connection>(newPoolCapacity);
        while (connectionPool.size() > newPoolCapacity) {
            val connection = connectionPool.take();
            if (!activeConnectionSet.contains(connection)) {
                connection.close();
            } else {
                newPool.offer(connection);
            }
        }
        connectionPool.drainTo(newPool);
        connectionPool = newPool;
        log.atInfo().log("Connection pool resized. New capacity: %s", newPoolCapacity);
    }

    /**
     * Handles high load scenarios by potentially expanding the pool.
     */
    private synchronized void handleHighLoad() {
        val highLoadThreshold = config.getHighLoadThreshold();
        val poolCapacityValue = poolCapacity.get();
        val poolUnderHighLoad = isPoolUnderHighLoad(poolCapacityValue, highLoadThreshold);
        if (!poolUnderHighLoad) return;
        val maximumPoolSize = config.getMaximumPoolSize();
        if (poolCapacityValue == maximumPoolSize) return;

        val poolGrowthFactor = config.getHighLoadPoolGrowthFactor();
        val newPoolCapacity = Math.min((int) (poolCapacityValue * poolGrowthFactor), maximumPoolSize);

        if (newPoolCapacity > poolCapacityValue) {
            growPool(newPoolCapacity);
        }
    }


    /**
     * Checks if the pool is under high load based on the current capacity and load threshold.
     *
     * @param poolCapacityValue The current capacity of the pool.
     * @param highLoadThreshold The threshold to determine high load.
     * @return True if the pool is under high load, false otherwise.
     */
    private boolean isPoolUnderHighLoad(int poolCapacityValue, double highLoadThreshold) {
        return (double) activeConnections.get() / poolCapacityValue > highLoadThreshold;
    }

    /**
     * Expands the connection pool to a new, larger capacity.
     *
     * @param newPoolCapacity The new capacity for the pool.
     */
    private void growPool(int newPoolCapacity) {
        poolCapacity.set(newPoolCapacity);
        val newPool = new ArrayBlockingQueue<Connection>(newPoolCapacity);
        connectionPool.drainTo(newPool);
        connectionPool = newPool;
        log.atInfo().log("Connection pool resized. New capacity: %s", newPoolCapacity);
    }

    /**
     * Schedules the connection leak detection task if it's not already scheduled.
     */
    private void scheduleLeakDetection() {
        if (leakDetectionScheduled) return;
        val interval = config.getConnectionLeakDetectorServiceInterval();
        val intervalUnit = config.getConnectionLeakDetectorServiceIntervalUnit();
        leakDetectionService.schedule(leakDetector, interval, intervalUnit);
        leakDetectionScheduled = true;
    }

    /**
     * Fills the connection pool with a specified number of new connections.
     *
     * @param numberOfConnections The number of new connections to add to the pool.
     * @throws SQLException If a database access error occurs while creating new connections.
     */
    private void fillConnectionPool(int numberOfConnections) throws SQLException {
        log.atFinest().log("Adding %d connection(s) to the pool...", numberOfConnections);
        for (var i = 0; i < numberOfConnections && connectionPool.size() < poolCapacity.get(); i++) {
            connectionPool.add(createNewConnection());
        }
    }

    /**
     * Creates a new database connection using the provided credentials.
     *
     * @return A new database connection.
     * @throws SQLException If a database access error occurs.
     */
    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(credentials.getBaseDatabaseUrl(), credentials.getUsername(), credentials.getPassword());
    }

    /**
     * Validates a database connection. Closes and replaces the connection if it is not valid.
     *
     * @param connection The database connection to validate.
     * @return The validated (or new replacement) connection.
     * @throws SQLException If a database access error occurs.
     */
    private Connection validateConnection(Connection connection) throws SQLException {
        if (!connection.isValid(config.getConnectionValidationTimeoutSeconds())) {
            log.atFine().log("Connection is no longer valid. Closing...");
            leakDetector.deregisterConnection(connection);
            connection.close();
            connection = createNewConnection();
        }
        return connection;
    }

    private synchronized void topUpConnections() throws SQLException {
        val maxNumberOfNewConnections = config.getMaximumConnectionGrowthAmount();
        var numberOfNewConnections = (int) (poolCapacity.get() * config.getHighLoadConnectionGrowthFactor());
        numberOfNewConnections = Math.min(numberOfNewConnections, maxNumberOfNewConnections);
        log.atInfo().log("Adding %d new connections to pool.", numberOfNewConnections);
        fillConnectionPool(numberOfNewConnections);
    }
}
