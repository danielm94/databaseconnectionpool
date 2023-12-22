package com.github.danielm94;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import lombok.val;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Class responsible for detecting and handling leaky database connections.
 * <p>
 * Author: Daniel Martins
 */
@Flogger
public class ConnectionLeakDetector implements Runnable {
    private final Map<Connection, Long> outgoingConnectionPool = new HashMap<>();
    private final Duration leakThreshold;

    /**
     * Constructs a new ConnectionLeakDetector with a specified threshold for determining leaks.
     *
     * @param leakThreshold The duration after which a connection is considered leaky if not returned.
     */
    public ConnectionLeakDetector(Duration leakThreshold) {
        this.leakThreshold = leakThreshold;
    }

    /**
     * Registers a new connection with the current time to track its usage duration.
     *
     * @param connection The connection to register.
     */
    public void registerConnection(@NonNull Connection connection) {
        val now = System.currentTimeMillis();
        outgoingConnectionPool.put(connection, now);
    }

    /**
     * Deregisters a connection from tracking, typically when it is returned to the pool.
     *
     * @param connection The connection to deregister.
     */
    public void deregisterConnection(@NonNull Connection connection) {
        outgoingConnectionPool.remove(connection);
    }

    /**
     * Checks all registered connections for leaks. If a connection is considered leaky,
     * it is handled appropriately and deregistered.
     *
     * @throws SQLException If an SQL exception occurs during the handling of a leaky connection.
     */
    public synchronized void checkForLeaks() throws SQLException {
        val connectionsToCull = new HashSet<Connection>();
        for (val key : outgoingConnectionPool.keySet()) {
            val now = System.currentTimeMillis();
            val threshold = leakThreshold.toMillis();
            val outgoingDuration = now - outgoingConnectionPool.get(key);
            if (outgoingDuration > threshold) {
                log.atWarning()
                   .log("Detected a leak! Connection in use for %dms, exceeding threshold of %dms. Closing connection.",
                           outgoingDuration, threshold);
                ConnectionPoolManager.getInstance().handleLeakyConnections(key);
                connectionsToCull.add(key);
            }
        }
        connectionsToCull.forEach(this::deregisterConnection);
    }

    /**
     * The run method for the Runnable interface. Periodically checks for leaks.
     */
    @SneakyThrows
    @Override
    public void run() {
        checkForLeaks();
    }
}
