package com.campusict;

import com.campusict.dao.TicketDAO;
import com.campusict.dao.UserDAO;
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
import java.util.List;
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
 * Provides the Swing interface for reporting new ICT faults.
 */
public class ReportFaultFrame extends JFrame {

    private static final int LOCATION_MAX_LENGTH = 100;
    private static final String DEFAULT_URGENCY = "Medium";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MainMenuFrame mainMenuFrame;
    private final UserDAO userDAO = new UserDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final JTextField ticketIdField = new JTextField();
    private final JComboBox<User> reporterComboBox = new JComboBox<>();
    private final JComboBox<String> categoryComboBox = new JComboBox<>(new String[]{
            "Wi-Fi / Network", "Hardware", "Software", "Lab Equipment", "Other"
    });
    private final JTextField locationField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(5, 30);
    private final JComboBox<String> urgencyComboBox = new JComboBox<>(new String[]{
            "Low", "Medium", "High", "Critical"
    });
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Ticket ID", "Reporter Name", "Category", "Location", "Urgency", "Status", "Date Reported"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable ticketsTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");
    private final JButton submitButton = new JButton("Submit Fault");
    private final JButton updateButton = new JButton("Update Ticket");
    private final JButton deleteButton = new JButton("Delete Ticket");
    private final JButton clearButton = new JButton("Clear");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton backButton = new JButton("Back to Main Menu");

    /**
     * Builds the Report Fault window and loads reporters and tickets in the background.
     *
     * @param mainMenuFrame existing menu window to show again when this form closes
     */
    public ReportFaultFrame(MainMenuFrame mainMenuFrame) {
        this.mainMenuFrame = mainMenuFrame;
        setTitle("Report Fault - Campus ICT Support System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1050, 740);
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
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Report Fault", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
        contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        setContentPane(contentPanel);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(createFaultDetailsPanel(), BorderLayout.NORTH);
        centerPanel.add(createTicketsTablePanel(), BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createFaultDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Fault Details"));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        urgencyComboBox.setSelectedItem(DEFAULT_URGENCY);
        reporterComboBox.setRenderer(new ReporterRenderer());
        ticketIdField.setEditable(false);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(detailsPanel, constraints, 0, "Selected Ticket ID:", ticketIdField);
        addFormRow(detailsPanel, constraints, 1, "Reporter:*", reporterComboBox);
        addFormRow(detailsPanel, constraints, 2, "Category:*", categoryComboBox);
        addFormRow(detailsPanel, constraints, 3, "Location:*", locationField);
        addFormRow(detailsPanel, constraints, 4, "Description:*", new JScrollPane(descriptionArea));
        addFormRow(detailsPanel, constraints, 5, "Urgency:*", urgencyComboBox);
        return detailsPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints constraints, int row, String labelText,
                            Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.weighty = 0;
        panel.add(new JLabel(labelText), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = component instanceof JScrollPane ? 1 : 0;
        panel.add(component, constraints);
    }

    private JPanel createTicketsTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Submitted Tickets"));
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketsTable.setAutoCreateRowSorter(true);
        ticketsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedTicket();
            }
        });
        tablePanel.add(new JScrollPane(ticketsTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionsPanel.add(submitButton);
        actionsPanel.add(updateButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(clearButton);
        actionsPanel.add(refreshButton);
        actionsPanel.add(backButton);

        submitButton.addActionListener(event -> submitFault());
        updateButton.addActionListener(event -> updateTicket());
        deleteButton.addActionListener(event -> deleteTicket());
        clearButton.addActionListener(event -> clearForm());
        refreshButton.addActionListener(event -> refreshData());
        backButton.addActionListener(event -> returnToMainMenu());

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(actionsPanel, BorderLayout.CENTER);
        return footerPanel;
    }

    private void refreshData() {
        setBusy(true, "Loading reporters and tickets...");
        new SwingWorker<ReportFaultData, Void>() {
            @Override
            protected ReportFaultData doInBackground() throws Exception {
                return new ReportFaultData(userDAO.findAll(), ticketDAO.findAllWithReporterNames());
            }

            @Override
            protected void done() {
                try {
                    ReportFaultData data = get();
                    displayReporters(data.reporters());
                    displayTickets(data.tickets());
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                } finally {
                    if (reporterComboBox.getItemCount() == 0) {
                        setBusy(false, "No users are available. Register a reporter in Users Management before submitting a fault.");
                    } else {
                        setBusy(false, "Showing " + tableModel.getRowCount() + " ticket(s).");
                    }
                }
            }
        }.execute();
    }

    private void submitFault() {
        Ticket ticket = createValidatedTicket(null);
        if (ticket == null) {
            return;
        }

        setBusy(true, "Submitting fault...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ticketDAO.insert(ticket);
            }

            @Override
            protected void done() {
                try {
                    int ticketId = get();
                    clearForm();
                    JOptionPane.showMessageDialog(ReportFaultFrame.this,
                            "Fault submitted successfully with Ticket ID " + ticketId + ".",
                            "Fault Submitted", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception exception) {
                    Throwable cause = unwrapException(exception);
                    if (isMissingReporterError(cause)) {
                        JOptionPane.showMessageDialog(ReportFaultFrame.this,
                                "The selected reporter no longer exists. The reporter list will now be reloaded. "
                                        + "Register or select a valid user before submitting the fault.",
                                "Reporter No Longer Available", JOptionPane.WARNING_MESSAGE);
                        refreshData();
                    } else {
                        showDatabaseError(cause);
                        setBusy(false, "Ready");
                    }
                }
            }
        }.execute();
    }

    private void updateTicket() {
        Integer selectedTicketId = getSelectedTicketIdFromTable();
        if (selectedTicketId == null) {
            showSelectionMessage("Please select a ticket to update.");
            return;
        }

        Ticket ticket = createValidatedTicket(selectedTicketId);
        if (ticket == null) {
            return;
        }

        setBusy(true, "Updating ticket...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ticketDAO.updateReportFields(ticket);
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        showMissingTicketMessage();
                    } else {
                        JOptionPane.showMessageDialog(ReportFaultFrame.this,
                                "Ticket " + selectedTicketId + " updated successfully.",
                                "Ticket Updated", JOptionPane.INFORMATION_MESSAGE);
                    }
                    clearForm();
                    refreshData();
                } catch (Exception exception) {
                    Throwable cause = unwrapException(exception);
                    if (isMissingReporterError(cause)) {
                        showMissingReporterMessage();
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
                    } else {
                        JOptionPane.showMessageDialog(ReportFaultFrame.this,
                                "Ticket " + selectedTicketId + " deleted successfully.",
                                "Ticket Deleted", JOptionPane.INFORMATION_MESSAGE);
                    }
                    clearForm();
                    refreshData();
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private Ticket createValidatedTicket(Integer ticketId) {
        User reporter = (User) reporterComboBox.getSelectedItem();
        String category = (String) categoryComboBox.getSelectedItem();
        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        String urgency = (String) urgencyComboBox.getSelectedItem();

        if (reporter == null) {
            showValidationMessage("A reporter is required. Register a user in Users Management first.");
            return null;
        }
        if (!isValidCategory(category)) {
            showValidationMessage("Select a valid fault category.");
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
        if (!isValidUrgency(urgency)) {
            showValidationMessage("Select a valid urgency level.");
            return null;
        }
        return new Ticket(ticketId, reporter.getUserId(), reporter.getFullName(), category, location,
                description, urgency, "Pending", null);
    }

    private void loadSelectedTicket() {
        Integer selectedTicketId = getSelectedTicketIdFromTable();
        if (selectedTicketId == null) {
            return;
        }

        setBusy(true, "Loading selected ticket...");
        new SwingWorker<Ticket, Void>() {
            @Override
            protected Ticket doInBackground() throws Exception {
                List<Ticket> tickets = ticketDAO.search(selectedTicketId, null, null, null);
                return tickets.isEmpty() ? null : tickets.get(0);
            }

            @Override
            protected void done() {
                try {
                    Ticket ticket = get();
                    if (ticket == null) {
                        showMissingTicketMessage();
                        clearForm();
                        refreshData();
                        return;
                    }
                    displaySelectedTicket(ticket);
                    statusLabel.setText("Selected Ticket ID " + ticket.getTicketId() + ".");
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                } finally {
                    setBusy(false, statusLabel.getText());
                }
            }
        }.execute();
    }

    private void displaySelectedTicket(Ticket ticket) {
        ticketIdField.setText(ticket.getTicketId().toString());
        selectReporter(ticket.getReporterId());
        categoryComboBox.setSelectedItem(ticket.getCategory());
        locationField.setText(ticket.getLocation());
        descriptionArea.setText(ticket.getDescription());
        urgencyComboBox.setSelectedItem(ticket.getUrgency());
    }

    private void selectReporter(Integer reporterId) {
        for (int index = 0; index < reporterComboBox.getItemCount(); index++) {
            User reporter = reporterComboBox.getItemAt(index);
            if (reporterId.equals(reporter.getUserId())) {
                reporterComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private Integer getSelectedTicketIdFromTable() {
        int selectedViewRow = ticketsTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return null;
        }
        int selectedModelRow = ticketsTable.convertRowIndexToModel(selectedViewRow);
        return (Integer) tableModel.getValueAt(selectedModelRow, 0);
    }

    private boolean isValidCategory(String category) {
        return "Wi-Fi / Network".equals(category) || "Hardware".equals(category) || "Software".equals(category)
                || "Lab Equipment".equals(category) || "Other".equals(category);
    }

    private boolean isValidUrgency(String urgency) {
        return "Low".equals(urgency) || "Medium".equals(urgency) || "High".equals(urgency)
                || "Critical".equals(urgency);
    }

    private void displayReporters(List<User> reporters) {
        reporterComboBox.removeAllItems();
        for (User reporter : reporters) {
            reporterComboBox.addItem(reporter);
        }
    }

    private void displayTickets(List<Ticket> tickets) {
        tableModel.setRowCount(0);
        for (Ticket ticket : tickets) {
            tableModel.addRow(new Object[]{
                    ticket.getTicketId(),
                    ticket.getReporterName(),
                    ticket.getCategory(),
                    ticket.getLocation(),
                    ticket.getUrgency(),
                    ticket.getStatus(),
                    ticket.getDateReported() == null ? "" : DATE_TIME_FORMATTER.format(ticket.getDateReported())
            });
        }
    }

    private void clearForm() {
        ticketsTable.clearSelection();
        ticketIdField.setText("");
        if (reporterComboBox.getItemCount() > 0) {
            reporterComboBox.setSelectedIndex(0);
        }
        categoryComboBox.setSelectedIndex(0);
        locationField.setText("");
        descriptionArea.setText("");
        urgencyComboBox.setSelectedItem(DEFAULT_URGENCY);
        statusLabel.setText("Form cleared. No records were changed.");
        locationField.requestFocusInWindow();
    }

    private void returnToMainMenu() {
        mainMenuFrame.setVisible(true);
        dispose();
    }

    private void setBusy(boolean busy, String message) {
        statusLabel.setText(message);
        reporterComboBox.setEnabled(!busy);
        categoryComboBox.setEnabled(!busy);
        locationField.setEnabled(!busy);
        descriptionArea.setEnabled(!busy);
        urgencyComboBox.setEnabled(!busy);
        ticketsTable.setEnabled(!busy);
        submitButton.setEnabled(!busy && reporterComboBox.getItemCount() > 0);
        updateButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        backButton.setEnabled(!busy);
    }

    private void showValidationMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showSelectionMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "No Ticket Selected", JOptionPane.WARNING_MESSAGE);
    }

    private void showMissingTicketMessage() {
        JOptionPane.showMessageDialog(this,
                "The selected ticket no longer exists. Ticket records will be reloaded.",
                "Ticket No Longer Available", JOptionPane.WARNING_MESSAGE);
    }

    private void showMissingReporterMessage() {
        JOptionPane.showMessageDialog(this,
                "The selected reporter no longer exists. Ticket records will be reloaded.",
                "Reporter No Longer Available", JOptionPane.WARNING_MESSAGE);
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

    private boolean isMissingReporterError(Throwable exception) {
        return exception instanceof SQLException sqlException
                && "23000".equals(sqlException.getSQLState())
                && sqlException.getErrorCode() == 1452;
    }

    private Throwable unwrapException(Exception exception) {
        return exception.getCause() == null ? exception : exception.getCause();
    }

    private record ReportFaultData(List<User> reporters, List<Ticket> tickets) {
    }

    /**
     * Displays each User object in the reporter dropdown as its ID and full name.
     */
    private static class ReporterRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof User user) {
                setText(user.getUserId() + " - " + user.getFullName());
            }
            return this;
        }
    }
}