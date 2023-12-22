package com.github.danielm94.config;

import java.time.Duration;

public interface ConnectionPoolConfiguration {

    int getMaximumPoolSize();
    int getInitialPoolSize();
    Duration getConnectionTimeout();
}
