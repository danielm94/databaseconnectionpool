package com.github.danielm94;

import com.github.danielm94.config.ConnectionPoolConfiguration;
import com.github.danielm94.config.DefaultPoolConfiguration;
import com.github.danielm94.config.PropertyFileConnectionPoolConfiguration;
import com.github.danielm94.credentials.ConnectionCredentials;
import com.github.danielm94.credentials.RawConnectionCredentials;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionPoolManagerTest {
    private static final ConnectionCredentials H2_CREDENTIALS = new RawConnectionCredentials("", "sa", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    private static final ConnectionPoolConfiguration DEFAULT_CONFIG = new DefaultPoolConfiguration();
    public static final Class<ConnectionPoolManager> CONNECTION_POOL_MANAGER_CLASS = ConnectionPoolManager.class;
    public static final String INSTANCE_FIELD_NAME = "instance";

    @Mock
    private ConnectionCredentials mockCredentials;

    @Mock
    private ConnectionPoolConfiguration mockConfig;

    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(CONNECTION_POOL_MANAGER_CLASS, INSTANCE_FIELD_NAME);
    }

    @Test
    void initializeShouldThrowExceptionIfNullConfigPassedTest() {
        assertThrows(NullPointerException.class, () -> ConnectionPoolManager.initialize(null, H2_CREDENTIALS));
    }

    @Test
    void initializeShouldThrowExceptionIfNullCredentialsPassedTest() {
        assertThrows(NullPointerException.class, () -> ConnectionPoolManager.initialize(DEFAULT_CONFIG, null));
    }

    @Test
    void getInstanceShouldThrowExceptionIfSingletonNotInitializedTest() {
        assertThrows(IllegalStateException.class, ConnectionPoolManager::getInstance);
    }

    @Test
    void getConnectionShouldReturnValidConnectionTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val connection = ConnectionPoolManager.getInstance().getConnection();

        assertNotNull(connection, "getConnection should return a non-null connection");
        connection.close();
    }

    @Test
    void getConnectionShouldValidateConnectionFromPoolTest() throws Exception {
        val config = getConfigurationFromPropertyFile(
                "src/test/resources/com/github/danielm94/ConnectionPoolManagerTest/get-connection-should-validate-connection-from-pool-test-config.properties");
        ConnectionPoolManager.initialize(config, H2_CREDENTIALS);

        val mockConnection = mock(Connection.class);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        insertMockConnectionIntoPool(mockConnection);

        val connection = ConnectionPoolManager.getInstance().getConnection();
        verify(connection, times(1)).isValid(anyInt());
    }

    @Test
    void getConnectionShouldTopUpWhenEmptyTest() throws SQLException, IOException, InterruptedException {
        val config = getConfigurationFromPropertyFile(
                "src/test/resources/com/github/danielm94/ConnectionPoolManagerTest/get-connection-should-top-up-when-empty-test-config.properties");
        ConnectionPoolManager.initialize(config, H2_CREDENTIALS);
        val manager = ConnectionPoolManager.getInstance();
        val initialSize = manager.getFreeConnections();
        val connection = manager.getConnection();
        val currentSize = manager.getFreeConnections();
        val poolAddedNewConnections = currentSize > initialSize;
        val failureMessage = String.format(
                "Connection pool did not add a new connection when the pool was empty! " +
                        "Initial size: %d | Size after requesting connection: %d | Initial number of connections in pool from config: %d"
                , initialSize, currentSize, config.getInitialPoolSize());
        assertTrue(poolAddedNewConnections, failureMessage);
        if (connection != null) connection.close();
    }

    @Test
    void getConnectionPoolShouldResizeWhenHighLoadReachedTest() throws IOException, SQLException, InterruptedException {
        val config = getConfigurationFromPropertyFile(
                "src/test/resources/com/github/danielm94/ConnectionPoolManagerTest/get-connection-pool-should-resize-when-high-load-reached-test-config.properties");
        ConnectionPoolManager.initialize(config, H2_CREDENTIALS);

        val manager = ConnectionPoolManager.getInstance();
        val initialSize = manager.getPoolCapacity();

        manager.getConnection();
        manager.getConnection();
        val currentSize = manager.getPoolCapacity();
        val message = String.format("Connection pool didn't resize after requesting a connection under high load conditions. Initial Pool Size: %d | Current Pool Size: %d", initialSize, currentSize);
        assertTrue(currentSize > initialSize, message);
    }

    @Test
    void getConnectionActiveConnectionsShouldIncrementWhenValidConnectionHandedTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val manager = ConnectionPoolManager.getInstance();
        val initialActiveConnections = manager.getActiveConnections();
        val connection = manager.getConnection();
        val currentActiveConnections = manager.getActiveConnections();
        val expectedDifference = 1;
        val actualDifference = currentActiveConnections - initialActiveConnections;
        val message = String.format("Expected the amount of active connections to increment by %d when successfully requesting a connection from pool! " +
                        "Initial Active Connections: %d | Current Active Connections: %d",
                expectedDifference, initialActiveConnections, currentActiveConnections);
        assertEquals(expectedDifference, actualDifference, message);
        if (connection != null) connection.close();
    }

    @Test
    void getConnectionShouldMarkConnectionAsActiveWhenHandedOutTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val manager = ConnectionPoolManager.getInstance();
        val connection = manager.getConnection();

        val isActive = manager.isConnectionActive(connection);
        assertTrue(isActive, "Connection should have been marked as active, but it wasn't!");
    }

    @Test
    void getConnectionShouldRegisterConnectionWithLeakDetectorTest() throws Exception {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);

        val mockLeakDetector = mock(ConnectionLeakDetector.class);
        insertMockLeakDetector(mockLeakDetector);

        val connection = ConnectionPoolManager.getInstance().getConnection();
        verify(mockLeakDetector, times(1)).registerConnection(connection);

        connection.close();
    }

    @Test
    void getConnectionShouldScheduleLeakDetectionAfterGettingConnectionTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);


        val manager = ConnectionPoolManager.getInstance();
        val connection = manager.getConnection();

        assertTrue(manager.isLeakDetectionServiceScheduled());
        if (connection != null) connection.close();
    }

    @Test
    void returnConnectionShouldValidateConnectionTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val mockConnection = mock(Connection.class);
        when(mockConnection.isValid(anyInt())).thenReturn(true);

        ConnectionPoolManager.getInstance().returnConnection(mockConnection);

        verify(mockConnection, times(1)).isValid(anyInt());
    }

    @Test
    void returnConnectionShouldThrowNullPointerExceptionIfNullConnectionPassedTest() throws SQLException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        assertThrows(NullPointerException.class, () -> ConnectionPoolManager.getInstance().returnConnection(null));
    }

    @Test
    void returnConnectionShouldOfferValidConnectionToPoolTest() throws
            SQLException, InterruptedException, IOException {
        val config = getConfigurationFromPropertyFile(
                "src/test/resources/com/github/danielm94/ConnectionPoolManagerTest/return-connection-should-offer-connection-to-pool-test-config.properties");
        ConnectionPoolManager.initialize(config, H2_CREDENTIALS);

        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(anyInt())).thenReturn(true);

        val manager = ConnectionPoolManager.getInstance();
        manager.returnConnection(mockConnection);
        val actualPoolSize = manager.getFreeConnections();
        val expectedPoolSize = 1;
        val failureMessage = String.format("Expected the pool size to be %d after returning a valid connection, but the actual size was %d!", expectedPoolSize, actualPoolSize);
        assertEquals(expectedPoolSize, actualPoolSize, failureMessage);
    }

    @Test
    void returnConnectionShouldMarkConnectionAsInactiveTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val manager = ConnectionPoolManager.getInstance();
        val connection = manager.getConnection();

        val initialActiveState = manager.isConnectionActive(connection);
        assertTrue(initialActiveState, "The connection should be initially active after getting it from the pool.");

        manager.returnConnection(connection);

        val currentActiveState = manager.isConnectionActive(connection);
        assertFalse(currentActiveState, "The connection should be marked as inactive after returning it to the pool.");
    }

    @Test
    void returnConnectionShouldDecrementNumberOfActiveConnectionsTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val manager = ConnectionPoolManager.getInstance();
        val connection = manager.getConnection();

        val initialActiveConnections = manager.getActiveConnections();

        manager.returnConnection(connection);

        val currentActiveConnections = manager.getActiveConnections();
        val expectedDifference = 1;
        val actualDifference = initialActiveConnections - currentActiveConnections;
        val failMessage = String.format("The amount of active connections should have decremented by %d, but it did not! " +
                        "Initial active connections: %d | Actual active connections: %d",
                expectedDifference, initialActiveConnections, currentActiveConnections);
        assertEquals(expectedDifference, actualDifference, failMessage);
    }

    @Test
    void returnConnectionShouldCloseInvalidConnectionsTest() throws SQLException, InterruptedException {
        ConnectionPoolManager.initialize(DEFAULT_CONFIG, H2_CREDENTIALS);
        val manager = ConnectionPoolManager.getInstance();

        val connection = mock(Connection.class);
        when(connection.isValid(anyInt())).thenReturn(false);

        manager.returnConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    void returnConnectionShouldDetectLowLoadAndResizeTest() throws IOException, SQLException, InterruptedException {
        val config = getConfigurationFromPropertyFile(
                "src/test/resources/com/github/danielm94/ConnectionPoolManagerTest/return-connection-should-detect-low-load-and-resize-test-config.properties");
        ConnectionPoolManager.initialize(config, H2_CREDENTIALS);

        val manager = ConnectionPoolManager.getInstance();

        val connection = manager.getConnection();
        val initialSize = manager.getPoolCapacity();
        manager.returnConnection(connection);
        val currentSize = manager.getPoolCapacity();
        assertTrue(currentSize < initialSize);

    }

    private ConnectionPoolConfiguration getConfigurationFromPropertyFile(String propertyFilePath) throws
            IOException {
        val propertyFile = new FileInputStream(propertyFilePath);
        return new PropertyFileConnectionPoolConfiguration(propertyFile);
    }

    private static void resetSingleton(Class clazz, String fieldName) throws ReflectiveOperationException {
        val instance = clazz.getDeclaredField(fieldName);
        instance.setAccessible(true);
        instance.set(null, null);
    }


    private void insertMockConnectionIntoPool(Connection mockConnection) throws Exception {
        // Access the singleton instance
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();

        // Access the private connectionPool field
        Field poolField = ConnectionPoolManager.class.getDeclaredField("connectionPool");
        poolField.setAccessible(true);

        // Get the actual connection pool
        @SuppressWarnings("unchecked")
        BlockingQueue<Connection> connectionPool = (BlockingQueue<Connection>) poolField.get(manager);

        // Insert the mock connection
        connectionPool.offer(mockConnection);
    }

    private void insertMockLeakDetector(ConnectionLeakDetector mockLeakDetector) throws Exception {
        // Access the singleton instance
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();

        // Access the private leakDetector field
        Field detectorField = ConnectionPoolManager.class.getDeclaredField("leakDetector");
        detectorField.setAccessible(true);

        // Set the mock leak detector
        detectorField.set(manager, mockLeakDetector);
    }

}