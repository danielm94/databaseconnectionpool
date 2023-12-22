package com.github.danielm94.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DefaultPoolConfiguration implements ConnectionPoolConfiguration {
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 50;

    public static final int INITIAL_POOL_SIZE = 10;

    public static final int CONNECTION_VALIDATION = 5;
    public static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.of(10, ChronoUnit.MINUTES);

    @Override
    public int getMaximumPoolSize() {
        return DEFAULT_MAXIMUM_POOL_SIZE;
    }

    @Override
    public int getInitialPoolSize() {
        return INITIAL_POOL_SIZE;
    }

    @Override
    public Duration getConnectionTimeout() {
        return DEFAULT_CONNECTION_TIMEOUT;
    }
}
