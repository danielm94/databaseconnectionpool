package com.github.danielm94;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Author: Daniel Martins
 */
class ConnectionLeakDetectorTest {
    @Test
    void registerConnectionShouldAddToTracking() {
        val mockConnection = mock(Connection.class);
        val detector = new ConnectionLeakDetector(Duration.ofMinutes(5));

        detector.registerConnection(mockConnection);

        assertTrue(detector.isConnectionRegistered(mockConnection));
    }

    @Test
    void deregisterConnectionShouldRemoveFromTracking() {
        val mockConnection = mock(Connection.class);
        val detector = new ConnectionLeakDetector(Duration.ofMinutes(5));

        detector.registerConnection(mockConnection);
        detector.deregisterConnection(mockConnection);

        assertFalse(detector.isConnectionRegistered(mockConnection));
    }

    @Test
    void registerConnectionShouldThrowExceptionForNull() {
        val detector = new ConnectionLeakDetector(Duration.ofMinutes(5));

        assertThrows(NullPointerException.class, () -> detector.registerConnection(null));
    }

    @Test
    void deregisterConnectionShouldThrowExceptionForNull() {
        val detector = new ConnectionLeakDetector(Duration.ofMinutes(5));

        assertThrows(NullPointerException.class, () -> detector.deregisterConnection(null));
    }

    @Test
    void isConnectionRegisteredShouldThrowExceptionForNull() {
        val detector = new ConnectionLeakDetector(Duration.ofMinutes(5));

        assertThrows(NullPointerException.class, () -> detector.isConnectionRegistered(null));
    }

    @Test
    void checkForLeaksShouldHandleLeakyConnection() throws SQLException, InterruptedException {
        try (MockedStatic<ConnectionPoolManager> mocked = mockStatic(ConnectionPoolManager.class)) {
            val mockConnection = mock(Connection.class);
            val detector = new ConnectionLeakDetector(Duration.ofMillis(1));
            val mockManager = mock(ConnectionPoolManager.class);

            mocked.when(ConnectionPoolManager::getInstance).thenReturn(mockManager);
            doNothing().when(mockManager).handleLeakyConnections(any(Connection.class));

            detector.registerConnection(mockConnection);
            Thread.sleep(10);

            detector.checkForLeaks();

            verify(mockManager, times(1)).handleLeakyConnections(mockConnection);
            assertFalse(detector.isConnectionRegistered(mockConnection));
        }
    }

    @Test
    void checkForLeaksShouldNotHandleNonLeakyConnection() throws SQLException {
        try (val mocked = mockStatic(ConnectionPoolManager.class)) {
            val mockConnection = mock(Connection.class);
            val detector = new ConnectionLeakDetector(Duration.ofMinutes(5));
            val mockManager = mock(ConnectionPoolManager.class);

            mocked.when(ConnectionPoolManager::getInstance).thenReturn(mockManager);
            doNothing().when(mockManager).handleLeakyConnections(any(Connection.class));

            detector.registerConnection(mockConnection);
            detector.checkForLeaks();

            verify(mockManager, never()).handleLeakyConnections(mockConnection);
            assertTrue(detector.isConnectionRegistered(mockConnection));
        }
    }

    @Test
    void checkForLeaksShouldHandleMultipleConnections() throws SQLException, InterruptedException {
        try (MockedStatic<ConnectionPoolManager> mocked = mockStatic(ConnectionPoolManager.class)) {
            val mockConnectionLeaky = mock(Connection.class);
            val mockConnectionSafe = mock(Connection.class);

            val detector = new ConnectionLeakDetector(Duration.ofSeconds(4));
            val mockManager = mock(ConnectionPoolManager.class);

            mocked.when(ConnectionPoolManager::getInstance).thenReturn(mockManager);
            doNothing().when(mockManager).handleLeakyConnections(any(Connection.class));

            detector.registerConnection(mockConnectionLeaky);

            Thread.sleep(5000);

            detector.registerConnection(mockConnectionSafe);

            detector.checkForLeaks();
            verify(mockManager, times(1)).handleLeakyConnections(mockConnectionLeaky);
            assertFalse(detector.isConnectionRegistered(mockConnectionLeaky));
            assertTrue(detector.isConnectionRegistered(mockConnectionSafe));
        }
    }

    @Test
    void checkForLeaksShouldContinueOnException() throws SQLException, InterruptedException {
        try (MockedStatic<ConnectionPoolManager> mocked = mockStatic(ConnectionPoolManager.class)) {
            val mockConnectionLeaky = mock(Connection.class);
            val mockConnectionSafe = mock(Connection.class);
            val detector = new ConnectionLeakDetector(Duration.ofSeconds(2));
            val mockManager = mock(ConnectionPoolManager.class);

            mocked.when(ConnectionPoolManager::getInstance).thenReturn(mockManager);

            doThrow(SQLException.class).when(mockManager).handleLeakyConnections(mockConnectionLeaky);

            detector.registerConnection(mockConnectionLeaky);
            Thread.sleep(3000);
            detector.registerConnection(mockConnectionSafe);

            detector.checkForLeaks();

            assertTrue(detector.isConnectionRegistered(mockConnectionSafe));
            verify(mockManager, times(1)).handleLeakyConnections(mockConnectionLeaky);
        }
    }
}