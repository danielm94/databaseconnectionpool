package com.github.danielm94.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface ConnectionPoolConfiguration {

    int getInitialMaxPoolSize();
    int getInitialPoolSize();
    Duration getConnectionFromPoolTimeout();

    Duration getConnectionLeakThreshold();

    int getConnectionValidationTimeoutSeconds();

    long getConnectionLeakDetectorServiceInterval();
    TimeUnit getConnectionLeakDetectorServiceIntervalUnit();

    double getHighLoadThreshold();

    double getLowLoadThreshold();

    int getMaximumPoolSize();

    double getHighLoadPoolGrowthFactor();

    double getHighLoadConnectionGrowthFactor();

    int getMaximumConnectionGrowthAmount();

    double getLowLoadPoolShrinkFactor();

    int getLowLoadHysteresisCount();
}
