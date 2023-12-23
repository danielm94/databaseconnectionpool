package com.github.danielm94.config;

import com.github.danielm94.exceptions.MissingPropertyException;
import lombok.NonNull;
import lombok.val;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Provides an implementation of {@link ConnectionPoolConfiguration} by reading configuration values from a properties file.
 * This class configures connection pool properties such as initial and maximum pool sizes, connection timeout settings,
 * leak detection thresholds, and load thresholds for the pool, among others.
 * <p>
 * The configuration values are read from a standard Java properties file. This implementation
 * ensures that all required properties are present and correctly formatted, throwing a {@link MissingPropertyException}
 * if any required property is missing or invalid.
 * <p>
 * Usage:
 * <pre>
 * FileInputStream propertyFile = new FileInputStream("path/to/config.properties");
 * PropertyFileConnectionPoolConfiguration config = new PropertyFileConnectionPoolConfiguration(propertyFile);
 * </pre>
 * Author: Daniel Martins
 */
public class PropertyFileConnectionPoolConfiguration implements ConnectionPoolConfiguration {
    private final Properties properties;
    private static final String INITIAL_MAX_POOL_SIZE = "initial.max.pool.size";
    private static final String INITIAL_POOL_SIZE = "initial.pool.size";
    private static final String CONNECTION_TIMEOUT_AMOUNT = "connection.timeout.amount";
    private static final String CONNECTION_TIMEOUT_UNIT = "connection.timeout.unit";
    private static final String CONNECTION_LEAK_THRESHOLD_AMOUNT = "connection.leak.threshold.amount";
    private static final String CONNECTION_LEAK_THRESHOLD_UNIT = "connection.leak.threshold.unit";
    private static final String CONNECTION_VALIDATION_TIMEOUT_SECONDS = "connection.validation.timeout.seconds";
    private static final String CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL = "connection.leak.detector.service.interval";
    private static final String CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL_UNIT = "connection.leak.detector.service.interval.unit";
    private static final String HIGH_LOAD_THRESHOLD = "high.load.threshold";
    private static final String LOW_LOAD_THRESHOLD = "low.load.threshold";
    private static final String MAXIMUM_POOL_SIZE = "maximum.pool.size";
    private static final String HIGH_LOAD_GROWTH_FACTOR = "high.load.growth.factor";
    private static final String HIGH_LOAD_CONNECTION_GROWTH_FACTOR = "high.load.connection.growth.factor";
    private static final String MAXIMUM_CONNECTION_GROWTH_AMOUNT = "maximum.connection.growth.amount";
    private static final String LOW_LOAD_POOL_SHRINK_FACTOR = "low.load.pool.shrink.factor";
    private static final String LOW_LOAD_HYSTERESIS_COUNT = "low.load.hysteresis.count";

    /**
     * Constructs a new {@code PropertyFileConnectionPoolConfiguration} instance by loading
     * connection pool configuration properties from the provided file input stream.
     *
     * <p>This constructor reads the connection pool configuration from a properties file
     * represented by a {@link FileInputStream}. The properties file should contain
     * key-value pairs defining various configuration settings for the connection pool,
     * such as pool sizes, timeout settings, and thresholds for load handling.</p>
     *
     * <p>Example properties might include the initial maximum pool size, connection
     * validation timeout, and load thresholds. Each property key should match the
     * predefined static constants in this class.</p>
     *
     * @param propertyFile The file input stream of the properties file containing
     *                     connection pool configurations. This parameter must not be null.
     * @throws IOException          If an error occurs while reading from the {@code propertyFile}.
     * @throws NullPointerException If {@code propertyFile} is null.
     */
    public PropertyFileConnectionPoolConfiguration(@NonNull FileInputStream propertyFile) throws IOException {
        this.properties = new Properties();
        properties.load(propertyFile);
    }

    @Override
    public int getInitialMaxPoolSize() {
        val property = getAndValidateProperty(INITIAL_MAX_POOL_SIZE);
        return Integer.parseInt(property);
    }

    @Override
    public int getInitialPoolSize() {
        val property = getAndValidateProperty(INITIAL_POOL_SIZE);
        return Integer.parseInt(property);
    }

    @Override
    public Duration getConnectionFromPoolTimeout() {
        val connectionTimeoutAmountStr = getAndValidateProperty(CONNECTION_TIMEOUT_AMOUNT);
        val connectionTimeoutAmount = Integer.parseInt(connectionTimeoutAmountStr);
        val connectionTimeoutUnitStr = getAndValidateProperty(CONNECTION_TIMEOUT_UNIT);
        val connectionTimeoutUnit = ChronoUnit.valueOf(connectionTimeoutUnitStr);
        return Duration.of(connectionTimeoutAmount, connectionTimeoutUnit);
    }

    @Override
    public Duration getConnectionLeakThreshold() {
        val connectionLeakAmountStr = getAndValidateProperty(CONNECTION_LEAK_THRESHOLD_AMOUNT);
        val connectionLeakAmount = Integer.parseInt(connectionLeakAmountStr);
        val connectionLeakThresholdStr = getAndValidateProperty(CONNECTION_LEAK_THRESHOLD_UNIT);
        val connectionLeakThresholdUnit = ChronoUnit.valueOf(connectionLeakThresholdStr);
        return Duration.of(connectionLeakAmount, connectionLeakThresholdUnit);
    }

    @Override
    public int getConnectionValidationTimeoutSeconds() {
        val property = getAndValidateProperty(CONNECTION_VALIDATION_TIMEOUT_SECONDS);
        return Integer.parseInt(property);
    }

    @Override
    public long getConnectionLeakDetectorServiceInterval() {
        val property = getAndValidateProperty(CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL);
        return Long.parseLong(property);
    }

    @Override
    public TimeUnit getConnectionLeakDetectorServiceIntervalUnit() {
        val property = getAndValidateProperty(CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL_UNIT);
        return TimeUnit.valueOf(property.toUpperCase());
    }

    @Override
    public double getHighLoadThreshold() {
        val property = getAndValidateProperty(HIGH_LOAD_THRESHOLD);
        return Double.parseDouble(property);
    }

    @Override
    public double getLowLoadThreshold() {
        val property = getAndValidateProperty(LOW_LOAD_THRESHOLD);
        return Double.parseDouble(property);
    }

    @Override
    public int getMaximumPoolSize() {
        val property = getAndValidateProperty(MAXIMUM_POOL_SIZE);
        return Integer.parseInt(property);
    }

    @Override
    public double getHighLoadPoolGrowthFactor() {
        val property = getAndValidateProperty(HIGH_LOAD_GROWTH_FACTOR);
        return Double.parseDouble(property);
    }

    @Override
    public double getHighLoadConnectionGrowthFactor() {
        val property = getAndValidateProperty(HIGH_LOAD_CONNECTION_GROWTH_FACTOR);
        return Double.parseDouble(property);
    }

    @Override
    public int getMaximumConnectionGrowthAmount() {
        val property = getAndValidateProperty(MAXIMUM_CONNECTION_GROWTH_AMOUNT);
        return Integer.parseInt(property);
    }

    @Override
    public double getLowLoadPoolShrinkFactor() {
        val property = getAndValidateProperty(LOW_LOAD_POOL_SHRINK_FACTOR);
        return Double.parseDouble(property);
    }

    @Override
    public int getLowLoadHysteresisCount() {
        val property = getAndValidateProperty(LOW_LOAD_HYSTERESIS_COUNT);
        return Integer.parseInt(property);
    }

    private String getAndValidateProperty(String key) {
        val property = properties.getProperty(key);
        if (property == null) {
            throw new MissingPropertyException(String.format("Could not find %s key inside of property file.", key));
        }
        return property;
    }
}
