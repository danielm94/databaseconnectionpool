package com.github.danielm94.credentials;

/**
 * Interface defining the essential credentials required for establishing a database connection.
 *
 * @author Daniel Martins
 */
public interface ConnectionCredentials {

    /**
     * Retrieves the username used for database authentication.
     *
     * @return The username for the database connection.
     */
    String getUsername();

    /**
     * Retrieves the password used for database authentication.
     *
     * @return The password for the database connection.
     */
    String getPassword();

    /**
     * Retrieves the base URL for the database. This URL typically includes the database type,
     * host address, and may include additional parameters specific to the database.
     *
     * @return The base URL for connecting to the database.
     */
    String getBaseDatabaseUrl();
}
