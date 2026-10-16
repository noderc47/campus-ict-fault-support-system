package com.campusict;

import com.campusict.dao.TechnicianDAO;
import com.campusict.dao.TicketDAO;
import com.campusict.dao.UserDAO;
import com.campusict.model.Technician;
import com.campusict.model.Ticket;
import com.campusict.model.User;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Provides the final Swing form for searching, updating, and deleting tickets.
 */
public class SearchManageTicketsFrame extends JFrame {

    private static final String ALL_FILTER = "All";
    private static final int LOCATION_MAX_LENGTH = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MainMenuFrame mainMenuFrame;
    private final TicketDAO ticketDAO = new TicketDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TechnicianDAO technicianDAO = new TechnicianDAO();
    private final JTextField ticketIdSearchField = new JTextField(8);
    private final JTextField locationSearchField = new JTextField(16);
    private final JComboBox<String> categoryFilterComboBox = new JComboBox<>(new String[]{
            ALL_FILTER, "Wi-Fi / Network", "Hardware", "Software", "Lab Equipment", "Other"
    });
    private final JComboBox<String> statusFilterComboBox = new JComboBox<>(new String[]{
            ALL_FILTER, "Pending", "In Progress", "Resolved"
    });
    private final JTextField ticketIdField = new JTextField();
    private final JTextField reporterField = new JTextField();
    private final JTextField dateReportedField = new JTextField();
    private final JComboBox<String> categoryComboBox = new JComboBox<>(new String[]{
            "Wi-Fi / Network", "Hardware", "Software", "Lab Equipment", "Other"
    });
    private final JTextField locationField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(5, 30);
    private final JComboBox<String> urgencyComboBox = new JComboBox<>(new String[]{
            "Low", "Medium", "High", "Critical"
    });
    private final JComboBox<String> statusComboBox = new JComboBox<>(new String[]{
            "Pending", "In Progress", "Resolved"
    });
    private final JComboBox<TechnicianOption> technicianComboBox = new JComboBox<>();
    private final JTextArea resolutionNotesArea = new JTextArea(4, 30);
    private final JTextField resolvedAtField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Ticket ID", "Reporter Name", "Category", "Location", "Urgency", "Status", "Technician Name", "Date Reported"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable ticketsTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");
    private final JButton searchButton = new JButton("Search");
    private final JButton updateButton = new JButton("Update Ticket");
    private final JButton deleteButton = new JButton("Delete Ticket");
    private final JButton clearButton = new JButton("Clear");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton backButton = new JButton("Back to Main Menu");
    private final Map<Integer, Ticket> ticketsById = new HashMap<>();

    /**
     * Builds the ticket-management window and loads tickets, users, and technicians.
     *
     * @param mainMenuFrame existing menu window to show again when this form closes
     */
    public SearchManageTicketsFrame(MainMenuFrame mainMenuFrame) {
        this.mainMenuFrame = mainMenuFrame;
        setTitle("Search & Manage Tickets - Campus ICT Support System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 760));
        setSize(1180, 820);
        setLocationRelativeTo(mainMenuFrame);
        createUserInterface();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                returnToMainMenu();
            }
        });
        refreshData();
    }

    private void createUserInterface() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titleLabel = new JLabel("Search & Manage Tickets", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(createSearchPanel(), BorderLayout.NORTH);
        centerPanel.add(createTicketsTablePanel(), BorderLayout.CENTER);
        centerPanel.add(createEditingPanel(), BorderLayout.SOUTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        setContentPane(contentPanel);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Tickets"));
        searchPanel.add(new JLabel("Ticket ID:"));
        searchPanel.add(ticketIdSearchField);
        searchPanel.add(new JLabel("Location:"));
        searchPanel.add(locationSearchField);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryFilterComboBox);
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilterComboBox);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    private JPanel createTicketsTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Tickets"));
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketsTable.setAutoCreateRowSorter(true);
        ticketsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedTicketIntoFields();
            }
        });
        tablePanel.add(new JScrollPane(ticketsTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createEditingPanel() {
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Selected Ticket Details"));
        ticketIdField.setEditable(false);
        reporterField.setEditable(false);
        dateReportedField.setEditable(false);
        resolvedAtField.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        resolutionNotesArea.setLineWrap(true);
        resolutionNotesArea.setWrapStyleWord(true);
        technicianComboBox.setRenderer(new TechnicianRenderer());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 5, 4, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(detailsPanel, constraints, 0, 0, "Ticket ID:", ticketIdField);
        addFormRow(detailsPanel, constraints, 2, 0, "Reporter:", reporterField);
        addFormRow(detailsPanel, constraints, 0, 1, "Date Reported:", dateReportedField);
        addFormRow(detailsPanel, constraints, 2, 1, "Resolved At:", resolvedAtField);
        addFormRow(detailsPanel, constraints, 0, 2, "Category:*", categoryComboBox);
        addFormRow(detailsPanel, constraints, 2, 2, "Location:*", locationField);
        addFormRow(detailsPanel, constraints, 0, 3, "Urgency:*", urgencyComboBox);
        addFormRow(detailsPanel, constraints, 2, 3, "Status:*", statusComboBox);
        addFormRow(detailsPanel, constraints, 0, 4, "Technician:", technicianComboBox);
        addFormRow(detailsPanel, constraints, 2, 4, "Resolution Notes:", new JScrollPane(resolutionNotesArea));
        addFormRow(detailsPanel, constraints, 0, 5, "Description:*", new JScrollPane(descriptionArea));
        return detailsPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints constraints, int gridX, int gridY, String labelText,
                            Component component) {
        constraints.gridx = gridX;
        constraints.gridy = gridY;
        constraints.weightx = 0;
        constraints.weighty = 0;
        panel.add(new JLabel(labelText), constraints);
        constraints.gridx = gridX + 1;
        constraints.weightx = 1;
        constraints.weighty = component instanceof JScrollPane ? 1 : 0;
        constraints.gridwidth = gridY == 5 ? 3 : 1;
        panel.add(component, constraints);
        constraints.gridwidth = 1;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionsPanel.add(updateButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(clearButton);
        actionsPanel.add(refreshButton);
        actionsPanel.add(backButton);

        searchButton.addActionListener(event -> searchTickets());
        updateButton.addActionListener(event -> updateTicket());
        deleteButton.addActionListener(event -> deleteTicket());
        clearButton.addActionListener(event -> clearForm());
        refreshButton.addActionListener(event -> refreshData());
        backButton.addActionListener(event -> returnToMainMenu());
        ticketIdSearchField.addActionListener(event -> searchTickets());
        locationSearchField.addActionListener(event -> searchTickets());

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(actionsPanel, BorderLayout.CENTER);
        return footerPanel;
    }

    private void refreshData() {
        clearSearchCriteria();
        loadData(createSearchCriteria());
    }

    private void searchTickets() {
        SearchCriteria criteria = createSearchCriteria();
        if (criteria == null) {
            return;
        }
        loadData(criteria);
    }

    private SearchCriteria createSearchCriteria() {
        String ticketIdText = ticketIdSearchField.getText().trim();
        Integer ticketId = null;
        if (!ticketIdText.isEmpty()) {
            try {
                ticketId = Integer.valueOf(ticketIdText);
                if (ticketId <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                showValidationMessage("Ticket ID must be a positive whole number.");
                ticketIdSearchField.requestFocusInWindow();
                return null;
            }
        }
        return new SearchCriteria(ticketId, locationSearchField.getText().trim(),
                getFilterValue(categoryFilterComboBox), getFilterValue(statusFilterComboBox));
    }

    private String getFilterValue(JComboBox<String> comboBox) {
        String value = (String) comboBox.getSelectedItem();
        return ALL_FILTER.equals(value) ? null : value;
    }

    private void loadData(SearchCriteria criteria) {
        setBusy(true, "Loading tickets and technicians...");
        new SwingWorker<TicketManagementData, Void>() {
            @Override
            protected TicketManagementData doInBackground() throws Exception {
                List<Ticket> tickets = ticketDAO.search(criteria.ticketId(), criteria.location(),
                        criteria.category(), criteria.status());
                return new TicketManagementData(tickets, userDAO.findAll(), technicianDAO.findAll());
            }

            @Override
            protected void done() {
                try {
                    TicketManagementData data = get();
                    displayTickets(data.tickets());
                    displayTechnicians(data.technicians());
                    clearEditingFields();
                    if (data.tickets().isEmpty()) {
                        statusLabel.setText("No tickets match the selected search criteria.");
                    } else {
                        statusLabel.setText("Showing " + data.tickets().size() + " ticket(s).");
                    }
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                } finally {
                    setBusy(false, statusLabel.getText());
                }
            }
        }.execute();
    }

    private void updateTicket() {
        Ticket ticket = createValidatedTicketFromFields();
        if (ticket == null) {
            return;
        }

        setBusy(true, "Updating ticket...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ticketDAO.update(ticket);
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        showMissingTicketMessage();
                        refreshData();
                        return;
                    }
                    JOptionPane.showMessageDialog(SearchManageTicketsFrame.this,
                            "Ticket " + ticket.getTicketId() + " updated successfully.",
                            "Ticket Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadData(createSearchCriteria());
                } catch (Exception exception) {
                    Throwable cause = unwrapException(exception);
                    if (isMissingTechnicianError(cause)) {
                        JOptionPane.showMessageDialog(SearchManageTicketsFrame.this,
                                "The selected technician no longer exists. Technician choices will be reloaded.",
                                "Technician No Longer Available", JOptionPane.WARNING_MESSAGE);
                        refreshData();
                    } else {
                        showDatabaseError(cause);
                        setBusy(false, "Ready");
                    }
                }
            }
        }.execute();
    }

    private void deleteTicket() {
        Integer selectedTicketId = getSelectedTicketIdFromTable();
        if (selectedTicketId == null) {
            showSelectionMessage("Please select a record to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete Ticket ID " + selectedTicketId + "?\nThis cannot be undone.",
                "Confirm Ticket Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            statusLabel.setText("Deletion cancelled. No records were changed.");
            return;
        }

        setBusy(true, "Deleting ticket...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ticketDAO.delete(selectedTicketId);
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        showMissingTicketMessage();
                        clearEditingFields();
                        loadData(createSearchCriteria());
                        return;
                    }
                    JOptionPane.showMessageDialog(SearchManageTicketsFrame.this,
                            "Ticket " + selectedTicketId + " deleted successfully.",
                            "Ticket Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearEditingFields();
                    loadData(createSearchCriteria());
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private Ticket createValidatedTicketFromFields() {
        Integer ticketId = getSelectedTicketId();
        if (ticketId == null) {
            showSelectionMessage("Select a ticket row before updating.");
            return null;
        }

        String category = (String) categoryComboBox.getSelectedItem();
        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        String urgency = (String) urgencyComboBox.getSelectedItem();
        String status = (String) statusComboBox.getSelectedItem();
        TechnicianOption technicianOption = (TechnicianOption) technicianComboBox.getSelectedItem();
        String resolutionNotes = resolutionNotesArea.getText().trim();

        if (!isValidCategory(category)) {
            showValidationMessage("Select a valid category.");
            return null;
        }
        if (location.isEmpty()) {
            showValidationMessage("Location is required.");
            locationField.requestFocusInWindow();
            return null;
        }
        if (location.length() > LOCATION_MAX_LENGTH) {
            showValidationMessage("Location must be " + LOCATION_MAX_LENGTH + " characters or fewer.");
            return null;
        }
        if (description.isEmpty()) {
            showValidationMessage("Description is required.");
            descriptionArea.requestFocusInWindow();
            return null;
        }
        if (!isValidUrgency(urgency) || !isValidStatus(status)) {
            showValidationMessage("Select valid urgency and status values.");
            return null;
        }
        Integer technicianId = technicianOption == null ? null : technicianOption.techId();
        if ("In Progress".equals(status) && technicianId == null) {
            showValidationMessage("Assign a technician before setting a ticket to In Progress.");
            return null;
        }
        if ("Resolved".equals(status) && resolutionNotes.isBlank()) {
            showValidationMessage("Resolution notes are required before setting a ticket to Resolved.");
            resolutionNotesArea.requestFocusInWindow();
            return null;
        }

        Ticket selectedTicket = ticketsById.get(ticketId);
        return new Ticket(ticketId, selectedTicket.getReporterId(), selectedTicket.getReporterName(), category,
                location, description, urgency, status, technicianId,
                technicianOption == null ? null : technicianOption.techName(), resolutionNotes,
                selectedTicket.getResolvedAt(), selectedTicket.getDateReported(), selectedTicket.getUpdatedAt());
    }

    private void displayTickets(List<Ticket> tickets) {
        ticketsById.clear();
        tableModel.setRowCount(0);
        for (Ticket ticket : tickets) {
            ticketsById.put(ticket.getTicketId(), ticket);
            tableModel.addRow(new Object[]{
                    ticket.getTicketId(), ticket.getReporterName(), ticket.getCategory(), ticket.getLocation(),
                    ticket.getUrgency(), ticket.getStatus(),
                    ticket.getTechnicianName() == null ? "Unassigned" : ticket.getTechnicianName(),
                    formatDateTime(ticket.getDateReported())
            });
        }
    }

    private void displayTechnicians(List<Technician> technicians) {
        technicianComboBox.removeAllItems();
        technicianComboBox.addItem(new TechnicianOption(null, "Unassigned"));
        for (Technician technician : technicians) {
            technicianComboBox.addItem(new TechnicianOption(technician.getTechId(), technician.getTechName()));
        }
    }

    private void loadSelectedTicketIntoFields() {
        int selectedViewRow = ticketsTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return;
        }
        int selectedModelRow = ticketsTable.convertRowIndexToModel(selectedViewRow);
        Integer ticketId = (Integer) tableModel.getValueAt(selectedModelRow, 0);
        Ticket ticket = ticketsById.get(ticketId);
        if (ticket == null) {
            return;
        }
        ticketIdField.setText(ticket.getTicketId().toString());
        reporterField.setText(ticket.getReporterId() + " - " + ticket.getReporterName());
        dateReportedField.setText(formatDateTime(ticket.getDateReported()));
        categoryComboBox.setSelectedItem(ticket.getCategory());
        locationField.setText(ticket.getLocation());
        descriptionArea.setText(ticket.getDescription());
        urgencyComboBox.setSelectedItem(ticket.getUrgency());
        statusComboBox.setSelectedItem(ticket.getStatus());
        selectTechnician(ticket.getAssignedTechId());
        resolutionNotesArea.setText(ticket.getResolutionNotes() == null ? "" : ticket.getResolutionNotes());
        resolvedAtField.setText(formatDateTime(ticket.getResolvedAt()));
        statusLabel.setText("Selected Ticket ID " + ticket.getTicketId() + ".");
    }

    private void selectTechnician(Integer technicianId) {
        for (int index = 0; index < technicianComboBox.getItemCount(); index++) {
            TechnicianOption option = technicianComboBox.getItemAt(index);
            if ((technicianId == null && option.techId() == null)
                    || (technicianId != null && technicianId.equals(option.techId()))) {
                technicianComboBox.setSelectedIndex(index);
                return;
            }
        }
        technicianComboBox.setSelectedIndex(0);
    }

    private Integer getSelectedTicketId() {
        if (ticketIdField.getText().isBlank()) {
            return null;
        }
        return Integer.valueOf(ticketIdField.getText());
    }

    private Integer getSelectedTicketIdFromTable() {
        int selectedViewRow = ticketsTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return null;
        }
        int selectedModelRow = ticketsTable.convertRowIndexToModel(selectedViewRow);
        return (Integer) tableModel.getValueAt(selectedModelRow, 0);
    }

    private void clearForm() {
        clearSearchCriteria();
        clearEditingFields();
        statusLabel.setText("Form cleared. No records were changed.");
    }

    private void clearSearchCriteria() {
        ticketIdSearchField.setText("");
        locationSearchField.setText("");
        categoryFilterComboBox.setSelectedItem(ALL_FILTER);
        statusFilterComboBox.setSelectedItem(ALL_FILTER);
    }

    private void clearEditingFields() {
        ticketsTable.clearSelection();
        ticketIdField.setText("");
        reporterField.setText("");
        dateReportedField.setText("");
        categoryComboBox.setSelectedIndex(0);
        locationField.setText("");
        descriptionArea.setText("");
        urgencyComboBox.setSelectedItem("Medium");
        statusComboBox.setSelectedItem("Pending");
        if (technicianComboBox.getItemCount() > 0) {
            technicianComboBox.setSelectedIndex(0);
        }
        resolutionNotesArea.setText("");
        resolvedAtField.setText("");
    }

    private void returnToMainMenu() {
        mainMenuFrame.setVisible(true);
        dispose();
    }

    private void setBusy(boolean busy, String message) {
        statusLabel.setText(message);
        ticketIdSearchField.setEnabled(!busy);
        locationSearchField.setEnabled(!busy);
        categoryFilterComboBox.setEnabled(!busy);
        statusFilterComboBox.setEnabled(!busy);
        searchButton.setEnabled(!busy);
        ticketsTable.setEnabled(!busy);
        categoryComboBox.setEnabled(!busy);
        locationField.setEnabled(!busy);
        descriptionArea.setEnabled(!busy);
        urgencyComboBox.setEnabled(!busy);
        statusComboBox.setEnabled(!busy);
        technicianComboBox.setEnabled(!busy);
        resolutionNotesArea.setEnabled(!busy);
        updateButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        backButton.setEnabled(!busy);
    }

    private boolean isValidCategory(String category) {
        return "Wi-Fi / Network".equals(category) || "Hardware".equals(category) || "Software".equals(category)
                || "Lab Equipment".equals(category) || "Other".equals(category);
    }

    private boolean isValidUrgency(String urgency) {
        return "Low".equals(urgency) || "Medium".equals(urgency) || "High".equals(urgency)
                || "Critical".equals(urgency);
    }

    private boolean isValidStatus(String status) {
        return "Pending".equals(status) || "In Progress".equals(status) || "Resolved".equals(status);
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
    }

    private void showValidationMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showSelectionMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "No Ticket Selected", JOptionPane.WARNING_MESSAGE);
    }

    private void showMissingTicketMessage() {
        JOptionPane.showMessageDialog(this,
                "The selected ticket no longer exists. Ticket results will be reloaded.",
                "Ticket No Longer Available", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(Throwable exception) {
        if (exception instanceof IOException || exception instanceof IllegalStateException) {
            JOptionPane.showMessageDialog(this,
                    "The database configuration could not be loaded. Verify the local database settings and try again.",
                    "Database Configuration Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "The database operation could not be completed. Please try again or contact the system administrator.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        statusLabel.setText("Database operation failed.");
    }

    private boolean isMissingTechnicianError(Throwable exception) {
        return exception instanceof SQLException sqlException
                && "23000".equals(sqlException.getSQLState())
                && sqlException.getErrorCode() == 1452;
    }

    private Throwable unwrapException(Exception exception) {
        return exception.getCause() == null ? exception : exception.getCause();
    }

    private record SearchCriteria(Integer ticketId, String location, String category, String status) {
    }

    private record TicketManagementData(List<Ticket> tickets, List<User> reporters, List<Technician> technicians) {
    }

    private record TechnicianOption(Integer techId, String techName) {
    }

    /**
     * Displays the technician dropdown with an Unassigned option and ID-name pairs.
     */
    private static class TechnicianRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TechnicianOption option) {
                setText(option.techId() == null ? "Unassigned" : option.techId() + " - " + option.techName());
            }
            return this;
        }
    }
}