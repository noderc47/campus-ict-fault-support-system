package com.campusict.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections using credentials kept in an external properties file.
 */
public final class DatabaseConnection {

    private static final String CONFIG_PATH_PROPERTY = "db.config";
    private static final Path DEFAULT_CONFIG_PATH = Path.of("config", "db.properties");

    private DatabaseConnection() {
        // Prevents this utility class from being instantiated.
    }

    /**
     * Opens a connection using db.url, db.username, and db.password from the configured properties file.
     *
     * @return an open JDBC connection
     * @throws IOException if the configuration file cannot be read
     * @throws IllegalStateException if a required configuration property is missing
     * @throws SQLException if MySQL rejects or cannot establish the connection
     */
    public static Connection getConnection() throws IOException, SQLException {
        Properties properties = loadProperties();
        String url = getRequiredProperty(properties, "db.url");
        String username = getRequiredProperty(properties, "db.username");
        String password = getRequiredProperty(properties, "db.password");
        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties() throws IOException {
        Path configPath = getConfigPath();
        if (!Files.isRegularFile(configPath)) {
            throw new IOException("Database configuration file was not found.");
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        }
        return properties;
    }

    private static Path getConfigPath() {
        String configuredPath = System.getProperty(CONFIG_PATH_PROPERTY);
        return configuredPath == null || configuredPath.isBlank()
                ? DEFAULT_CONFIG_PATH
                : Path.of(configuredPath.trim());
    }

    private static String getRequiredProperty(Properties properties, String propertyName) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required database configuration is missing.");
        }
        return value.trim();
    }
}
