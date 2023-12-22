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
