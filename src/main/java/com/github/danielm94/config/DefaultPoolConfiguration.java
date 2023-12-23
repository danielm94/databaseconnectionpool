package com.github.danielm94.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class DefaultPoolConfiguration implements ConnectionPoolConfiguration {
    public static final int INITIAL_MAX_POOL_SIZE = 50;
    public static final int INITIAL_POOL_SIZE = 10;
    public static final int CONNECTION_VALIDATION_TIMEOUT_SECONDS = 10;
    public static final Duration CONNECTION_TIMEOUT = Duration.of(5, ChronoUnit.SECONDS);
    public static final Duration CONNECTION_LEAK_THRESHOLD = Duration.of(5, ChronoUnit.MINUTES);
    public static final int CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL = 2;
    public static final TimeUnit CONNECTION_LEAK_DETECTOR_SERVICE_INTERVAL_UNIT = TimeUnit.MINUTES;
    public static final double HIGH_LOAD_THRESHOLD = 0.9;
    public static final int MAXIMUM_POOL_SIZE = 125;
    public static final double HIGH_LOAD_GROWTH_FACTOR = 1.5;
    public static final int LOW_LOAD_HYSTERESIS_COUNT = 5;
    public static final double HIGH_LOAD_CONNECTION_GROWTH_FACTOR = 0.1;
    public static final double LOW_LOAD_POOL_SHRINK_FACTOR = 0.5;
    public static final int MAXIMUM_CONNECTION_GROWTH_AMOUNT = 8;
    public static final double LOW_LOAD_THRESHOLD = 0.15;


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
