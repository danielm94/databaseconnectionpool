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
        if (username == null) logCouldNotFindKeyInProps(USER_KEY);
        return username;
    }

    @Override
    public String getPassword() {
        val password = properties.getProperty(PASSWORD_KEY);
        if (password == null) logCouldNotFindKeyInProps(PASSWORD_KEY);
        return password;
    }

    @Override
    public String getBaseDatabaseUrl() {
        val url = properties.getProperty(URL_KEY);
        if (url == null) logCouldNotFindKeyInProps(URL_KEY);
        return url;
    }

    private static void logCouldNotFindKeyInProps(String key) {
        log.atWarning()
           .log("Could not find %s key inside of property file. Returning null.", key);
    }
}
