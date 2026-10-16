package com.campusict.dao;

import com.campusict.database.DatabaseConnection;
import com.campusict.model.Ticket;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs JDBC database operations for the tickets table used by Report Fault.
 */
public class TicketDAO {

    /**
     * Inserts a newly reported fault and returns the MySQL-generated ticket ID.
     *
     * @param ticket fault details to save
     * @return generated ticket ID
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public int insert(Ticket ticket) throws IOException, SQLException {
        String query = "INSERT INTO tickets (reporter_id, category, location, description, urgency, status, "
                + "assigned_tech_id, resolution_notes, resolved_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, ticket.getReporterId());
            statement.setString(2, ticket.getCategory());
            statement.setString(3, ticket.getLocation());
            statement.setString(4, ticket.getDescription());
            statement.setString(5, ticket.getUrgency());
            statement.setString(6, "Pending");
            statement.setNull(7, Types.INTEGER);
            statement.setNull(8, Types.LONGVARCHAR);
            statement.setNull(9, Types.TIMESTAMP);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("MySQL did not return a generated ticket ID.");
    }

    /**
     * Returns tickets with the reporter's name from the users table.
     *
     * @return all tickets, newest first
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public List<Ticket> findAllWithReporterNames() throws IOException, SQLException {
        return search(null, null, null, null);
    }

    /**
     * Finds tickets using optional criteria joined with AND.
     *
     * @param ticketId exact ticket ID, or null for all IDs
     * @param location partial location text, or null for all locations
     * @param category exact category, or null for all categories
     * @param status exact status, or null for all statuses
     * @return tickets with reporter and optional technician names
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public List<Ticket> search(Integer ticketId, String location, String category, String status)
            throws IOException, SQLException {
        StringBuilder query = new StringBuilder("SELECT t.ticket_id, t.reporter_id, u.full_name AS reporter_name, ")
                .append("t.category, t.location, t.description, t.urgency, t.status, t.assigned_tech_id, ")
                .append("tech.tech_name AS technician_name, t.resolution_notes, t.resolved_at, ")
                .append("t.date_reported, t.updated_at ")
                .append("FROM tickets t INNER JOIN users u ON t.reporter_id = u.user_id ")
                .append("LEFT JOIN technicians tech ON t.assigned_tech_id = tech.tech_id WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (ticketId != null) {
            query.append(" AND t.ticket_id = ?");
            parameters.add(ticketId);
        }
        if (location != null && !location.isBlank()) {
            query.append(" AND t.location LIKE ?");
            parameters.add("%" + location.trim() + "%");
        }
        if (category != null && !category.isBlank()) {
            query.append(" AND t.category = ?");
            parameters.add(category);
        }
        if (status != null && !status.isBlank()) {
            query.append(" AND t.status = ?");
            parameters.add(status);
        }
        query.append(" ORDER BY t.date_reported DESC, t.ticket_id DESC");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query.toString())) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return readTickets(resultSet);
            }
        }
    }

    /**
     * Updates editable ticket fields. MySQL controls the resolution timestamp and updated_at timestamp.
     *
     * @param ticket ticket values to save, including its primary key
     * @return true when a ticket was updated
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean update(Ticket ticket) throws IOException, SQLException {
        String query = "UPDATE tickets SET category = ?, location = ?, description = ?, urgency = ?, status = ?, "
                + "assigned_tech_id = ?, resolution_notes = ?, resolved_at = "
                + "CASE WHEN ? = 'Resolved' THEN CASE WHEN resolved_at IS NULL THEN CURRENT_TIMESTAMP "
                + "ELSE resolved_at END ELSE NULL END WHERE ticket_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ticket.getCategory());
            statement.setString(2, ticket.getLocation());
            statement.setString(3, ticket.getDescription());
            statement.setString(4, ticket.getUrgency());
            statement.setString(5, ticket.getStatus());
            if (ticket.getAssignedTechId() == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setInt(6, ticket.getAssignedTechId());
            }
            if (ticket.getResolutionNotes() == null || ticket.getResolutionNotes().isBlank()) {
                statement.setNull(7, Types.LONGVARCHAR);
            } else {
                statement.setString(7, ticket.getResolutionNotes());
            }
            statement.setString(8, ticket.getStatus());
            statement.setInt(9, ticket.getTicketId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Updates only the report fields that are editable on the Report Fault form.
     * Status, technician assignment, resolution details, and report timestamp are preserved.
     *
     * @param ticket report values to save, including its primary key and reporter ID
     * @return true when a ticket was updated
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean updateReportFields(Ticket ticket) throws IOException, SQLException {
        String query = "UPDATE tickets SET reporter_id = ?, category = ?, location = ?, description = ?, urgency = ? "
                + "WHERE ticket_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, ticket.getReporterId());
            statement.setString(2, ticket.getCategory());
            statement.setString(3, ticket.getLocation());
            statement.setString(4, ticket.getDescription());
            statement.setString(5, ticket.getUrgency());
            statement.setInt(6, ticket.getTicketId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a ticket by primary key.
     *
     * @param ticketId selected ticket ID
     * @return true when a ticket was deleted
     * @throws IOException if db.properties cannot be read
     * @throws SQLException if the database operation fails
     */
    public boolean delete(int ticketId) throws IOException, SQLException {
        String query = "DELETE FROM tickets WHERE ticket_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, ticketId);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Ticket> readTickets(ResultSet resultSet) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        while (resultSet.next()) {
            tickets.add(new Ticket(
                    resultSet.getInt("ticket_id"),
                    resultSet.getInt("reporter_id"),
                    resultSet.getString("reporter_name"),
                    resultSet.getString("category"),
                    resultSet.getString("location"),
                    resultSet.getString("description"),
                    resultSet.getString("urgency"),
                    resultSet.getString("status"),
                    getNullableInteger(resultSet, "assigned_tech_id"),
                    resultSet.getString("technician_name"),
                    resultSet.getString("resolution_notes"),
                    toLocalDateTime(resultSet.getTimestamp("resolved_at")),
                    toLocalDateTime(resultSet.getTimestamp("date_reported")),
                    toLocalDateTime(resultSet.getTimestamp("updated_at"))
            ));
        }
        return tickets;
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}