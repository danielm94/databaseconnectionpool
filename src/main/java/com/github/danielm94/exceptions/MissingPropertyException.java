package com.github.danielm94.exceptions;

/**
 * Exception thrown when a required property is not found in a property file.
 * This is a custom exception class extending {@link RuntimeException}, indicating
 * an issue during the runtime when accessing properties from a configuration file.
 * <p>
 * This exception is typically thrown in scenarios where an expected configuration
 * property is missing, which is critical for the application's operation.
 * </p>
 *
 * @author Daniel Martins
 */
public class MissingPropertyException extends RuntimeException {
    /**
     * Constructs a new MissingPropertyException with the specified detail message.
     * The message provides additional information about the missing property, such as
     * its key or context of usage.
     *
     * @param message The detail message about the missing property. This message is saved
     *                for later retrieval by the {@link #getMessage()} method.
     */
    public MissingPropertyException(String message) {
        super(message);
    }
}
