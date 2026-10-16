package com.campusict.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * A command-line database health check. It does not open a Swing window.
 */
public final class DatabaseConnectionCheck {

    private static final List<String> REQUIRED_TABLES = List.of("users", "technicians", "tickets");

    private DatabaseConnectionCheck() {
        // Prevents this utility class from being instantiated.
    }

    /**
     * Runs SELECT 1 and confirms that each required application table exists.
     *
     * @param args command-line arguments; none are required
     */
    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            verifySelectOne(connection);
            verifyRequiredTables(connection);
            System.out.println("Database connection check passed.");
        } catch (IOException exception) {
            System.err.println("Database configuration error: " + exception.getMessage());
            System.exit(1);
        } catch (IllegalStateException exception) {
            System.err.println("Database configuration error: " + exception.getMessage());
            System.exit(1);
        } catch (SQLException exception) {
            printDatabaseError(exception);
            System.exit(1);
        }
    }

    private static void verifySelectOne(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            if (!resultSet.next() || resultSet.getInt(1) != 1) {
                throw new SQLException("The database did not return the expected result for SELECT 1.");
            }
        }
        System.out.println("PASS: Connected to MySQL and SELECT 1 succeeded.");
    }

    private static void verifyRequiredTables(Connection connection) throws SQLException {
        for (String tableName : REQUIRED_TABLES) {
            if (!tableExists(connection, tableName)) {
                throw new SQLException("Required table '" + tableName + "' was not found in the selected database.");
            }
            System.out.println("PASS: Required table '" + tableName + "' exists.");
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        String query = "SELECT 1 FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void printDatabaseError(SQLException exception) {
        String sqlState = exception.getSQLState();
        if (sqlState != null && sqlState.startsWith("08")) {
            System.err.println("Database connection error: Could not reach MySQL. Check that MySQL is running and that db.url has the correct host and port.");
        } else if ("28000".equals(sqlState)) {
            System.err.println("Database authentication error: Check db.username and db.password in the external configuration file.");
        } else {
            System.err.println("Database check failed: " + exception.getMessage());
        }
    }
}