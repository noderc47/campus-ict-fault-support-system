package com.campusict.dao;

import com.campusict.database.DatabaseConnection;
import com.campusict.model.Technician;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs JDBC database operations for the technicians table.
 */
public class TechnicianDAO {

    private static final String SELECT_COLUMNS = "tech_id, tech_name, department, email, status";

    /**
     * Returns every technician, ordered by primary key.
     *
     * @return all technicians stored in MySQL
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public List<Technician> findAll() throws IOException, SQLException {
        String query = "SELECT " + SELECT_COLUMNS + " FROM technicians ORDER BY tech_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return readTechnicians(resultSet);
        }
    }

    /**
     * Searches by an exact numeric technician ID, or by part of a name or email address.
     *
     * @param searchText text entered by the user
     * @return matching technicians, ordered by primary key
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public List<Technician> search(String searchText) throws IOException, SQLException {
        String trimmedSearchText = searchText.trim();
        if (trimmedSearchText.isEmpty()) {
            return findAll();
        }

        String likeValue = "%" + trimmedSearchText + "%";
        if (isPositiveInteger(trimmedSearchText)) {
            String query = "SELECT " + SELECT_COLUMNS + " FROM technicians "
                    + "WHERE tech_id = ? OR tech_name LIKE ? OR email LIKE ? ORDER BY tech_id";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, Integer.parseInt(trimmedSearchText));
                statement.setString(2, likeValue);
                statement.setString(3, likeValue);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return readTechnicians(resultSet);
                }
            }
        }

        String query = "SELECT " + SELECT_COLUMNS + " FROM technicians "
                + "WHERE tech_name LIKE ? OR email LIKE ? ORDER BY tech_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, likeValue);
            statement.setString(2, likeValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                return readTechnicians(resultSet);
            }
        }
    }

    /**
     * Inserts a technician and returns the MySQL-generated primary key.
     *
     * @param technician technician details to save
     * @return generated technician ID
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public int insert(Technician technician) throws IOException, SQLException {
        String query = "INSERT INTO technicians (tech_name, department, email, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setTechnicianValues(statement, technician);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("MySQL did not return a generated technician ID.");
    }

    /**
     * Updates an existing technician identified by its primary key.
     *
     * @param technician technician details, including a non-null technician ID
     * @return true when a row was updated
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean update(Technician technician) throws IOException, SQLException {
        String query = "UPDATE technicians SET tech_name = ?, department = ?, email = ?, status = ? WHERE tech_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setTechnicianValues(statement, technician);
            statement.setInt(5, technician.getTechId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Deletes one technician by primary key. Linked tickets become unassigned by the schema rule.
     *
     * @param techId primary key of the selected technician
     * @return true when a row was deleted
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean delete(int techId) throws IOException, SQLException {
        String query = "DELETE FROM technicians WHERE tech_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, techId);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Technician> readTechnicians(ResultSet resultSet) throws SQLException {
        List<Technician> technicians = new ArrayList<>();
        while (resultSet.next()) {
            technicians.add(new Technician(
                    resultSet.getInt("tech_id"),
                    resultSet.getString("tech_name"),
                    resultSet.getString("department"),
                    resultSet.getString("email"),
                    resultSet.getString("status")
            ));
        }
        return technicians;
    }

    private void setTechnicianValues(PreparedStatement statement, Technician technician) throws SQLException {
        statement.setString(1, technician.getTechName());
        statement.setString(2, technician.getDepartment());
        statement.setString(3, technician.getEmail());
        statement.setString(4, technician.getStatus());
    }

    private boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}