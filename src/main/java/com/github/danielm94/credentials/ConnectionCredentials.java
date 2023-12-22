package com.github.danielm94.credentials;

public interface ConnectionCredentials {
    String getUsername();
    String getPassword();
    String getBaseDatabaseUrl();
}
