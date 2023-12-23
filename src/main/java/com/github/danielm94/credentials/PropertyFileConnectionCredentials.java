package com.github.danielm94.credentials;

import com.github.danielm94.MissingPropertyException;
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
        return getAndValidateProperty(USER_KEY);
    }

    @Override
    public String getPassword() {
        return getAndValidateProperty(PASSWORD_KEY);
    }

    @Override
    public String getBaseDatabaseUrl() {
        return getAndValidateProperty(URL_KEY);
    }

    private String getAndValidateProperty(String key) {
        val property = properties.getProperty(key);
        if (property == null) {
            throw new MissingPropertyException(String.format("Could not find %s key inside of property file.", key));
        }
        return property;
    }
}
