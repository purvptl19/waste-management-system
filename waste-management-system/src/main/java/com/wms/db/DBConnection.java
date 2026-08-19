package com.wms.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException(
                "Oracle JDBC driver (ojdbc11) not found on classpath. " +
                "Make sure the dependency is present.", e);
        }
    }

    private DBConnection() { }

    /** Returns a fresh Connection. Caller is responsible for closing it (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Quick connectivity test used at app startup. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
