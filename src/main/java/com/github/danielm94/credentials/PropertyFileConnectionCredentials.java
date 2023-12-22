package com.github.danielm94.credentials;

import lombok.NonNull;
import lombok.extern.flogger.Flogger;
import lombok.val;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Flogger
public class PropertyFileConnectionCredentials implements ConnectionCredentials {
    public static final String USER_KEY = "user";
    public static final String PASSWORD_KEY = "password";
    public static final String URL_KEY = "url";
    private final Properties properties;

    public PropertyFileConnectionCredentials(@NonNull FileInputStream propertyFile) throws IOException {
        this.properties = new Properties();
        properties.load(propertyFile);
    }

    @Override
    public String getUsername() {
        val username = properties.getProperty(USER_KEY);
        validateProperty(username, USER_KEY);
        return username;
    }

    @Override
    public String getPassword() {
        val password = properties.getProperty(PASSWORD_KEY);
        validateProperty(password, PASSWORD_KEY);
        return password;
    }

    @Override
    public String getBaseDatabaseUrl() {
        val url = properties.getProperty(URL_KEY);
        validateProperty(url, URL_KEY);
        return url;
    }

    private static void validateProperty(String username, String userKey) {
        if (username == null) {
            throw new MissingPropertyException(String.format("Could not find %s key inside of property file.", userKey));
        }
    }
}
