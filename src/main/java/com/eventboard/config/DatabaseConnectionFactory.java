package com.eventboard.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Creates JDBC connections using settings from application.properties.
 */
public class DatabaseConnectionFactory {
    private static final String DB_URL;
    private static final String DB_USERNAME;
    private static final String DB_PASSWORD;

    static {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver not found", e);
        }

        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConnectionFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("application.properties not found");
            }
            properties.load(inputStream);
            DB_URL = getRequiredProperty(properties, "db.url");
            DB_USERNAME = getRequiredProperty(properties, "db.username");
            DB_PASSWORD = getRequiredProperty(properties, "db.password");
        } catch (IOException e) {
            throw new RuntimeException("Cannot load database properties: " + e.getMessage(), e);
        }
    }
    private DatabaseConnectionFactory() {
    }

    /**
     * Opens a new database connection.
     *
     * @return new JDBC connection
     * @throws SQLException when the database connection cannot be opened
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
    private static String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return value;
    }
}
