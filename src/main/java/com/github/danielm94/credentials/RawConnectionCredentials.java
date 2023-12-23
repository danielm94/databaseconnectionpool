package com.github.danielm94.credentials;

import lombok.NonNull;

/**
 * Implementation of {@link ConnectionCredentials} providing direct, plain-text storage
 * of database connection credentials. This class is useful for scenarios where
 * credentials are obtained dynamically or from a source other than a properties file.
 *
 * <p>This class stores the username, password, and database URL as plain strings.
 * It is important to handle instances of this class carefully to maintain security.</p>
 *
 * <p>Each field is marked with {@link NonNull} to ensure that null values are not
 * accepted, preventing inadvertent errors or misconfigurations.</p>
 *
 * @author Daniel Martins
 */
public class RawConnectionCredentials implements ConnectionCredentials {
    private final String userName;
    private final String passWord;
    private final String databaseUrl;

    /**
     * Constructs a new {@code RawConnectionCredentials} instance with the specified
     * username, password, and database URL.
     *
     * @param userName    The username for the database connection.
     * @param passWord    The password for the database connection.
     * @param databaseUrl The URL of the database to connect to.
     * @throws NullPointerException if any of the parameters are null.
     */
    public RawConnectionCredentials(@NonNull String userName, @NonNull String passWord, @NonNull String databaseUrl) {
        this.userName = userName;
        this.passWord = passWord;
        this.databaseUrl = databaseUrl;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPassword() {
        return passWord;
    }

    @Override
    public String getBaseDatabaseUrl() {
        return databaseUrl;
    }
}
