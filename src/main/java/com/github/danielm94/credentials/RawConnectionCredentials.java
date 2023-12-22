package com.github.danielm94.credentials;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RawConnectionCredentials implements ConnectionCredentials {
    private String userName;
    private String passWord;
    private String databaseUrl;

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getBaseDatabaseUrl() {
        return null;
    }
}
