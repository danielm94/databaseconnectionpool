package com.github.danielm94.credentials;

import com.github.danielm94.MissingPropertyException;
import lombok.NonNull;
import lombok.extern.flogger.Flogger;
import lombok.val;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Implementation of {@link ConnectionCredentials} that reads database connection
 * credentials from a property file. This class allows for externalized and flexible
 * management of database credentials.
 *
 * <p>It expects a property file with specific keys for the database username, password,
 * and URL. These keys are:</p>
 * <ul>
 *     <li>{@link #USER_KEY} for the database username.</li>
 *     <li>{@link #PASSWORD_KEY} for the database password.</li>
 *     <li>{@link #URL_KEY} for the database connection URL.</li>
 * </ul>
 *
 * <p>Failure to find any of these keys in the provided property file will result in a
 * {@link MissingPropertyException} being thrown.</p>
 *
 * <p>This class is particularly useful for applications that wish to separate their
 * configuration details from code, enhancing security and flexibility.</p>
 *
 * @author Daniel Martins
 */
@Flogger
public class PropertyFileConnectionCredentials implements ConnectionCredentials {
    private final String username;
    private final String password;
    private final String url;
    private static final String USER_KEY = "user";
    private static final String PASSWORD_KEY = "password";
    private static final String URL_KEY = "url";
    private final Properties properties;

    /**
     * Constructs a new {@code PropertyFileConnectionCredentials} instance.
     * Loads the database connection properties from the specified property file.
     *
     * @param propertyFile The input stream of the property file containing database credentials.
     * @throws IOException              If there is an issue reading from the property file.
     * @throws MissingPropertyException If required properties (username, password, URL) are not found.
     */
    public PropertyFileConnectionCredentials(@NonNull FileInputStream propertyFile) throws IOException {
        this.properties = new Properties();
        this.properties.load(propertyFile);
        this.username = getAndValidateProperty(USER_KEY);
        this.password = getAndValidateProperty(PASSWORD_KEY);
        this.url = getAndValidateProperty(URL_KEY);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBaseDatabaseUrl() {
        return url;
    }

    private String getAndValidateProperty(String key) {
        val property = properties.getProperty(key);
        if (property == null) {
            throw new MissingPropertyException(String.format("Could not find %s key inside of property file.", key));
        }
        return property;
    }
}
