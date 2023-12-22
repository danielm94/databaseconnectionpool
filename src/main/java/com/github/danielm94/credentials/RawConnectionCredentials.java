package com.github.danielm94.credentials;

import lombok.NonNull;

public class RawConnectionCredentials implements ConnectionCredentials {
    private final String userName;
    private final String passWord;
    private final String databaseUrl;

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
