package com.github.danielm94.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Defines the configuration specifications for a database connection pool.
 * This interface provides methods to access various configuration parameters
 * that control the behavior and properties of a connection pool. Implementations
 * of this interface can provide customized configurations for different scenarios
 * or environments.
 *
 * <p>Configuration parameters include:</p>
 * <ul>
 *     <li>Initial and maximum pool sizes to define the pool capacity boundaries.</li>
 *     <li>Connection timeout settings to manage how long a client can wait for a connection.</li>
 *     <li>Leak detection settings to monitor and handle potential connection leaks.</li>
 *     <li>Validation timeout for testing the validity of connections.</li>
 *     <li>Load management thresholds for dynamically resizing the pool based on the load.</li>
 *     <li>Growth factors and shrink factors to control how the pool adjusts its size under different load conditions.</li>
 *     <li>Hysteresis count to prevent frequent resizing under fluctuating loads.</li>
 * </ul>
 * <p>
 * Implementations of this interface should ensure that these parameters are
 * accessible and manageable, allowing for flexible and efficient connection pool management.
 *
 * @author Daniel Martins
 */
public interface ConnectionPoolConfiguration {

    /**
     * Gets the initial maximum size of the connection pool.
     * This value defines the upper limit on the number of connections that can be in the pool at startup.
     *
     * @return The initial maximum size of the connection pool.
     */
    int getInitialMaxPoolSize();

    /**
     * Gets the initial number of connections to be created in the pool at startup.
     *
     * @return The initial number of connections in the pool.
     */
    int getInitialPoolSize();

    /**
     * Gets the maximum duration to wait for a connection from the pool before timing out.
     *
     * @return The maximum duration to wait for a connection.
     */
    Duration getConnectionFromPoolTimeout();

    /**
     * Gets the duration after which a connection is considered leaky if not returned.
     *
     * @return The duration threshold for connection leak detection.
     */
    Duration getConnectionLeakThreshold();

    /**
     * Gets the maximum number of seconds to wait for a connection validation.
     *
     * @return The timeout duration in seconds for connection validation.
     */
    int getConnectionValidationTimeoutSeconds();

    /**
     * Gets the interval at which the connection leak detection service should run.
     *
     * @return The interval for the connection leak detection service.
     */
    long getConnectionLeakDetectorServiceInterval();

    /**
     * Gets the time unit for the connection leak detector service interval.
     *
     * @return The time unit for the leak detection service interval.
     */
    TimeUnit getConnectionLeakDetectorServiceIntervalUnit();

    /**
     * Gets the load threshold above which the pool is considered to be under high load.
     *
     * @return The high load threshold as a ratio of active connections to pool capacity.
     */
    double getHighLoadThreshold();

    /**
     * Gets the load threshold below which the pool is considered to be under low load.
     *
     * @return The low load threshold as a ratio of active connections to pool capacity.
     */
    double getLowLoadThreshold();

    /**
     * Gets the maximum number of connections that the pool can scale to.
     *
     * @return The maximum number of connections in the pool.
     */
    int getMaximumPoolSize();

    /**
     * Gets the factor by which the pool should grow when it is under high load.
     *
     * @return The growth factor for the pool under high load.
     */
    double getHighLoadPoolGrowthFactor();

    /**
     * Gets the factor by which to increase the number of connections when the pool is under high load.
     *
     * @return The growth factor for the number of connections under high load.
     */
    double getHighLoadConnectionGrowthFactor();

    /**
     * Gets the maximum number of new connections that can be added.
     *
     * @return The maximum number of connections to add during expansion.
     */
    int getMaximumConnectionGrowthAmount();

    /**
     * Gets the factor by which the pool should shrink when it is under low load.
     *
     * @return The shrink factor for the pool under low load.
     */
    double getLowLoadPoolShrinkFactor();

    /**
     * Gets the count of consecutive checks needed to confirm a low load situation before shrinking the pool.
     *
     * @return The count of consecutive low load checks for shrinking the pool.
     */
    int getLowLoadHysteresisCount();
}