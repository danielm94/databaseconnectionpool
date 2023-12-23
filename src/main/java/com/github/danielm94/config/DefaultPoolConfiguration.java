package com.github.danielm94.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Provides default configuration settings for a database connection pool.
 * This class implements the {@link ConnectionPoolConfiguration} interface and offers predefined
 * values for various pool parameters such as initial and maximum pool sizes, timeout settings,
 * leak detection thresholds, and load management thresholds.
 * <p>
 * This default configuration is designed to provide a balanced setup suitable for a variety of
 * use cases.
 * <p>
 * Author: Daniel Martins
 */
public class DefaultPoolConfiguration implements ConnectionPoolConfiguration {
    private static final int INITIAL_MAX_POOL_SIZE = 50;
    private static final int INITIAL_POOL_SIZE = 10;
    private static final Duration CONNECTION_TIMEOUT = Duration.of(5, ChronoUnit.SECONDS);
    private static final Duration CONNECTION_LEAK_THRESHOLD = Duration.of(5, ChronoUnit.MINUTES);
    private static final int CONNECTION_VALIDATION_TIMEOUT_SECONDS = 10;
    private static final int CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL = 2;
    private static final TimeUnit CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL_UNIT = TimeUnit.MINUTES;
    private static final double HIGH_LOAD_THRESHOLD = 0.9;
    private static final double LOW_LOAD_THRESHOLD = 0.15;
    private static final int MAXIMUM_POOL_SIZE = 125;
    private static final double HIGH_LOAD_GROWTH_FACTOR = 1.5;
    private static final double HIGH_LOAD_CONNECTION_GROWTH_FACTOR = 0.1;
    private static final int MAXIMUM_CONNECTION_GROWTH_AMOUNT = 8;
    private static final double LOW_LOAD_POOL_SHRINK_FACTOR = 0.5;
    private static final int LOW_LOAD_HYSTERESIS_COUNT = 5;

    @Override
    public int getInitialMaxPoolSize() {
        return INITIAL_MAX_POOL_SIZE;
    }

    @Override
    public int getInitialPoolSize() {
        return INITIAL_POOL_SIZE;
    }

    @Override
    public Duration getConnectionFromPoolTimeout() {
        return CONNECTION_TIMEOUT;
    }

    @Override
    public Duration getConnectionLeakThreshold() {
        return CONNECTION_LEAK_THRESHOLD;
    }

    @Override
    public int getConnectionValidationTimeoutSeconds() {
        return CONNECTION_VALIDATION_TIMEOUT_SECONDS;
    }

    @Override
    public long getConnectionLeakDetectorServiceInterval() {
        return CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL;
    }

    @Override
    public TimeUnit getConnectionLeakDetectorServiceIntervalUnit() {
        return CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL_UNIT;
    }

    @Override
    public double getHighLoadThreshold() {
        return HIGH_LOAD_THRESHOLD;
    }

    @Override
    public double getLowLoadThreshold() {
        return LOW_LOAD_THRESHOLD;
    }

    @Override
    public int getMaximumPoolSize() {
        return MAXIMUM_POOL_SIZE;
    }

    @Override
    public double getHighLoadPoolGrowthFactor() {
        return HIGH_LOAD_GROWTH_FACTOR;
    }

    @Override
    public double getHighLoadConnectionGrowthFactor() {
        return HIGH_LOAD_CONNECTION_GROWTH_FACTOR;
    }

    @Override
    public int getMaximumConnectionGrowthAmount() {
        return MAXIMUM_CONNECTION_GROWTH_AMOUNT;
    }

    @Override
    public double getLowLoadPoolShrinkFactor() {
        return LOW_LOAD_POOL_SHRINK_FACTOR;
    }

    @Override
    public int getLowLoadHysteresisCount() {
        return LOW_LOAD_HYSTERESIS_COUNT;
    }
}
