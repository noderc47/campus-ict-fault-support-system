package com.campusict.dao;

import com.campusict.database.DatabaseConnection;
import com.campusict.model.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs JDBC database operations for the users table.
 */
public class UserDAO {

    private static final String SELECT_COLUMNS = "user_id, full_name, email, phone, role";

    /**
     * Returns every user, ordered by primary key.
     *
     * @return all users stored in MySQL
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public List<User> findAll() throws IOException, SQLException {
        String query = "SELECT " + SELECT_COLUMNS + " FROM users ORDER BY user_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return readUsers(resultSet);
        }
    }

    /**
     * Searches by an exact numeric user ID, or by part of a name or email address.
     *
     * @param searchText text entered by the user
     * @return matching users, ordered by primary key
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public List<User> search(String searchText) throws IOException, SQLException {
        String trimmedSearchText = searchText.trim();
        if (trimmedSearchText.isEmpty()) {
            return findAll();
        }

        String likeValue = "%" + trimmedSearchText + "%";
        if (isPositiveInteger(trimmedSearchText)) {
            String query = "SELECT " + SELECT_COLUMNS + " FROM users "
                    + "WHERE user_id = ? OR full_name LIKE ? OR email LIKE ? ORDER BY user_id";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, Integer.parseInt(trimmedSearchText));
                statement.setString(2, likeValue);
                statement.setString(3, likeValue);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return readUsers(resultSet);
                }
            }
        }

        String query = "SELECT " + SELECT_COLUMNS + " FROM users "
                + "WHERE full_name LIKE ? OR email LIKE ? ORDER BY user_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, likeValue);
            statement.setString(2, likeValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                return readUsers(resultSet);
            }
        }
    }

    /**
     * Inserts a user and returns the MySQL-generated primary key.
     *
     * @param user user details to save
     * @return generated user ID
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public int insert(User user) throws IOException, SQLException {
        String query = "INSERT INTO users (full_name, email, phone, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setUserValues(statement, user);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("MySQL did not return a generated user ID.");
    }

    /**
     * Updates an existing user identified by its primary key.
     *
     * @param user user details, including a non-null user ID
     * @return true when a row was updated
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean update(User user) throws IOException, SQLException {
        String query = "UPDATE users SET full_name = ?, email = ?, phone = ?, role = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setUserValues(statement, user);
            statement.setInt(5, user.getUserId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Deletes one user by primary key.
     *
     * @param userId primary key of the selected user
     * @return true when a row was deleted
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean delete(int userId) throws IOException, SQLException {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private List<User> readUsers(ResultSet resultSet) throws SQLException {
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("full_name"),
                    resultSet.getString("email"),
                    resultSet.getString("phone"),
                    resultSet.getString("role")
            ));
        }
        return users;
    }

    private void setUserValues(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getFullName());
        statement.setString(2, user.getEmail());
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            statement.setNull(3, java.sql.Types.VARCHAR);
        } else {
            statement.setString(3, user.getPhone());
        }
        statement.setString(4, user.getRole());
    }

    private boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}