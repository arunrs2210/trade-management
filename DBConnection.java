package com.shnoor.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — singleton connection manager for PostgreSQL.
 * Loads credentials from db.properties on the classpath.
 */
public class DBConnection {

    private static DBConnection instance;
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("db.properties not found on classpath");
            props.load(in);
            Class.forName("org.postgresql.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {}

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) instance = new DBConnection();
            }
        }
        return instance;
    }

    /**
     * Opens and returns a new JDBC Connection.
     * Uses environment variables if available (for Railway), falls back to db.properties.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DATABASE_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Fallback to properties file if env vars not set
        if (dbUrl == null) dbUrl = props.getProperty("db.url");
        if (dbUsername == null) dbUsername = props.getProperty("db.username");
        if (dbPassword == null) dbPassword = props.getProperty("db.password");
        
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
}
