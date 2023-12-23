# Database Connection Pool

## Overview

The Database Connection Pool project provides a robust solution for managing database connections efficiently. It
includes features like connection leak detection, dynamic pool resizing under high and low load conditions, and
configurable properties for easy customization.

## Features

- **Connection Pool Management**: Efficient handling of database connections with dynamic resizing capabilities.
- **Leak Detection**: Monitors and handles potential connection leaks, ensuring resource optimization.
- **Configurable Settings**: Customizable settings through property files or direct configuration for flexibility and
  ease of use.
- **Unit Testing**: Thoroughly tested with unit tests ensuring reliability and stability.

## Project Structure

The project is organized as follows:

- `src`
   - `main/java/com/github/danielm94`
      - `ConnectionLeakDetector.java`: Responsible for detecting and handling leaky database connections.
      - `ConnectionPoolManager.java`: Manages the database connection pool.
      - `config`
         - `ConnectionPoolConfiguration.java`: Interface for pool configuration.
         - `DefaultPoolConfiguration.java`: Default implementation of the pool configuration.
         - `PropertyFileConnectionPoolConfiguration.java`: Pool configuration using property files.
      - `credentials`
         - `ConnectionCredentials.java`: Interface for database credentials.
         - `PropertyFileConnectionCredentials.java`: Credentials implementation using property files.
         - `RawConnectionCredentials.java`: Direct credentials implementation.
      - `exceptions`
         - `MissingPropertyException.java`: Custom exception for missing properties.
   - `test/java/com/github/danielm94`
      - `ConnectionLeakDetectorTest.java`: Unit tests for connection leak detection.
      - `ConnectionPoolManagerTest.java`: Unit tests for connection pool management.

## Configuration

The connection pool can be configured using property files or direct configuration. The configurations include initial
pool size, maximum pool size, load thresholds, connection timeout settings, and more.

## Usage

- Initialize the `ConnectionPoolManager` with desired credentials and configuration.
- Use `getConnection()` to retrieve a database connection from the pool.
- Return the connection to the pool using `returnConnection()` after use.
- The pool automatically handles resizing and leak detection based on the configured parameters.

## Dependencies

- Java JDK 8 or higher
- JUnit 5 for unit testing
- Mockito for mocking in tests

## License

This project is licensed under the MIT License.
