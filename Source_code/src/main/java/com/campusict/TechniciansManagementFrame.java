package com.campusict;

import com.campusict.dao.TechnicianDAO;
import com.campusict.model.Technician;
import java.awt.BorderLayout;
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
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Provides the Swing interface for creating and managing technicians.
 */
public class TechniciansManagementFrame extends JFrame {

    private static final int TECHNICIAN_NAME_MAX_LENGTH = 100;
    private static final int DEPARTMENT_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final String DEFAULT_DEPARTMENT = "ICT Support";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final MainMenuFrame mainMenuFrame;
    private final TechnicianDAO technicianDAO = new TechnicianDAO();
    private final JTextField technicianIdField = new JTextField();
    private final JTextField technicianNameField = new JTextField();
    private final JTextField departmentField = new JTextField(DEFAULT_DEPARTMENT);
    private final JTextField emailField = new JTextField();
    private final JComboBox<String> statusComboBox = new JComboBox<>(
            new String[]{"Available", "Busy", "On Leave"});
    private final JTextField searchField = new JTextField(24);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Technician ID", "Technician Name", "Department", "Email", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable techniciansTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");
    private final JButton addButton = new JButton("Add");
    private final JButton searchButton = new JButton("Search");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton backButton = new JButton("Back to Main Menu");

    /**
     * Builds the Technicians Management window and loads technicians in the background.
     *
     * @param mainMenuFrame existing menu window to show again when this form closes
     */
    public TechniciansManagementFrame(MainMenuFrame mainMenuFrame) {
        this.mainMenuFrame = mainMenuFrame;
        setTitle("Technicians Management - Campus ICT Support System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1000, 680);
        setLocationRelativeTo(mainMenuFrame);
        createUserInterface();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                returnToMainMenu();
            }
        });
        loadTechnicians();
    }

    private void createUserInterface() {
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Technicians Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
        contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        setContentPane(contentPanel);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(createDetailsPanel(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Technician Details"));
        technicianIdField.setEditable(false);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(detailsPanel, constraints, 0, "Technician ID:", technicianIdField);
        addFormRow(detailsPanel, constraints, 1, "Technician Name:*", technicianNameField);
        addFormRow(detailsPanel, constraints, 2, "Department:*", departmentField);
        addFormRow(detailsPanel, constraints, 3, "Email:*", emailField);
        addFormRow(detailsPanel, constraints, 4, "Status:*", statusComboBox);
        return detailsPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints constraints, int row, String labelText,
                            java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(labelText), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Technicians"));
        techniciansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        techniciansTable.setAutoCreateRowSorter(true);
        techniciansTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedTechnicianIntoFields();
            }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search ID, name, or email:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchField.addActionListener(event -> searchTechnicians());

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(techniciansTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionsPanel.add(addButton);
        actionsPanel.add(updateButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(clearButton);
        actionsPanel.add(refreshButton);
        actionsPanel.add(backButton);

        addButton.addActionListener(event -> addTechnician());
        searchButton.addActionListener(event -> searchTechnicians());
        updateButton.addActionListener(event -> updateTechnician());
        deleteButton.addActionListener(event -> deleteTechnician());
        clearButton.addActionListener(event -> clearForm());
        refreshButton.addActionListener(event -> loadTechnicians());
        backButton.addActionListener(event -> returnToMainMenu());

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(actionsPanel, BorderLayout.CENTER);
        return footerPanel;
    }

    private void loadTechnicians() {
        runTechnicianListWorker("Loading technicians...", technicianDAO::findAll);
    }

    private void searchTechnicians() {
        runTechnicianListWorker("Searching technicians...", () -> technicianDAO.search(searchField.getText()));
    }

    private void runTechnicianListWorker(String loadingMessage, TechnicianListTask task) {
        setBusy(true, loadingMessage);
        new SwingWorker<List<Technician>, Void>() {
            @Override
            protected List<Technician> doInBackground() throws Exception {
                return task.loadTechnicians();
            }

            @Override
            protected void done() {
                try {
                    displayTechnicians(get());
                    statusLabel.setText("Showing " + tableModel.getRowCount() + " technician(s).");
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                } finally {
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private void addTechnician() {
        Technician technician = createValidatedTechnician();
        if (technician == null) {
            return;
        }

        setBusy(true, "Adding technician...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return technicianDAO.insert(technician);
            }

            @Override
            protected void done() {
                try {
                    int techId = get();
                    clearForm();
                    JOptionPane.showMessageDialog(TechniciansManagementFrame.this,
                            "Technician added successfully with Technician ID " + techId + ".",
                            "Technician Added", JOptionPane.INFORMATION_MESSAGE);
                    loadTechnicians();
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private void updateTechnician() {
        Integer selectedTechId = getSelectedTechnicianId();
        if (selectedTechId == null) {
            showSelectionMessage("Select a technician row before updating.");
            return;
        }

        Technician technician = createValidatedTechnician();
        if (technician == null) {
            return;
        }
        technician.setTechId(selectedTechId);

        setBusy(true, "Updating technician...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return technicianDAO.update(technician);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(TechniciansManagementFrame.this,
                                "Technician updated successfully.", "Technician Updated", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadTechnicians();
                    } else {
                        showDatabaseError(new SQLException("The selected technician no longer exists."));
                        setBusy(false, "Ready");
                    }
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private void deleteTechnician() {
        Integer selectedTechId = getSelectedTechnicianIdFromTable();
        if (selectedTechId == null) {
            showSelectionMessage("Please select a record to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the selected technician (Technician ID " + selectedTechId + ")?\n"
                        + "Linked tickets will remain, but ON DELETE SET NULL will clear their technician assignment.\n"
                        + "This cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            statusLabel.setText("Deletion cancelled. No records were changed.");
            return;
        }

        setBusy(true, "Deleting technician...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return technicianDAO.delete(selectedTechId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(TechniciansManagementFrame.this,
                                "Technician deleted successfully. Linked tickets are now unassigned.",
                                "Technician Deleted", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadTechnicians();
                    } else {
                        showMissingTechnicianMessage();
                        clearForm();
                        loadTechnicians();
                    }
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private Technician createValidatedTechnician() {
        String techName = technicianNameField.getText().trim();
        String department = departmentField.getText().trim();
        String email = emailField.getText().trim();
        String status = (String) statusComboBox.getSelectedItem();

        if (techName.isEmpty()) {
            showValidationMessage("Technician name is required.");
            technicianNameField.requestFocusInWindow();
            return null;
        }
        if (techName.length() > TECHNICIAN_NAME_MAX_LENGTH) {
            showValidationMessage("Technician name must be " + TECHNICIAN_NAME_MAX_LENGTH + " characters or fewer.");
            return null;
        }
        if (department.isEmpty()) {
            showValidationMessage("Department is required.");
            departmentField.requestFocusInWindow();
            return null;
        }
        if (department.length() > DEPARTMENT_MAX_LENGTH) {
            showValidationMessage("Department must be " + DEPARTMENT_MAX_LENGTH + " characters or fewer.");
            return null;
        }
        if (email.isEmpty()) {
            showValidationMessage("Email is required.");
            emailField.requestFocusInWindow();
            return null;
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            showValidationMessage("Email must be " + EMAIL_MAX_LENGTH + " characters or fewer.");
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showValidationMessage("Enter a valid email address, for example name@example.com.");
            return null;
        }
        if (!"Available".equals(status) && !"Busy".equals(status) && !"On Leave".equals(status)) {
            showValidationMessage("Select Available, Busy, or On Leave for the status.");
            return null;
        }
        return new Technician(null, techName, department, email, status);
    }

    private void loadSelectedTechnicianIntoFields() {
        int selectedViewRow = techniciansTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return;
        }
        int selectedModelRow = techniciansTable.convertRowIndexToModel(selectedViewRow);
        technicianIdField.setText(tableModel.getValueAt(selectedModelRow, 0).toString());
        technicianNameField.setText(tableModel.getValueAt(selectedModelRow, 1).toString());
        departmentField.setText(tableModel.getValueAt(selectedModelRow, 2).toString());
        emailField.setText(tableModel.getValueAt(selectedModelRow, 3).toString());
        statusComboBox.setSelectedItem(tableModel.getValueAt(selectedModelRow, 4).toString());
        statusLabel.setText("Selected Technician ID " + technicianIdField.getText() + ".");
    }

    private Integer getSelectedTechnicianId() {
        if (technicianIdField.getText().isBlank()) {
            return null;
        }
        return Integer.valueOf(technicianIdField.getText());
    }

    private Integer getSelectedTechnicianIdFromTable() {
        int selectedViewRow = techniciansTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return null;
        }
        int selectedModelRow = techniciansTable.convertRowIndexToModel(selectedViewRow);
        return (Integer) tableModel.getValueAt(selectedModelRow, 0);
    }

    private void displayTechnicians(List<Technician> technicians) {
        tableModel.setRowCount(0);
        for (Technician technician : technicians) {
            tableModel.addRow(new Object[]{
                    technician.getTechId(), technician.getTechName(), technician.getDepartment(),
                    technician.getEmail(), technician.getStatus()
            });
        }
    }

    private void clearForm() {
        techniciansTable.clearSelection();
        technicianIdField.setText("");
        technicianNameField.setText("");
        departmentField.setText(DEFAULT_DEPARTMENT);
        emailField.setText("");
        statusComboBox.setSelectedItem("Available");
        statusLabel.setText("Form cleared. No records were changed.");
        technicianNameField.requestFocusInWindow();
    }

    private void returnToMainMenu() {
        mainMenuFrame.setVisible(true);
        dispose();
    }

    private void setBusy(boolean busy, String message) {
        statusLabel.setText(message);
        addButton.setEnabled(!busy);
        searchButton.setEnabled(!busy);
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
        JOptionPane.showMessageDialog(this, message, "No Technician Selected", JOptionPane.WARNING_MESSAGE);
    }

    private void showMissingTechnicianMessage() {
        JOptionPane.showMessageDialog(this,
                "The selected technician no longer exists. Technician records will be reloaded.",
                "Technician No Longer Available", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(Throwable exception) {
        if (isDuplicateEmailError(exception)) {
            JOptionPane.showMessageDialog(this, "That email address is already assigned to another technician.",
                    "Duplicate Email", JOptionPane.WARNING_MESSAGE);
        } else if (exception instanceof IOException || exception instanceof IllegalStateException) {
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

    private boolean isDuplicateEmailError(Throwable exception) {
        return exception instanceof SQLException sqlException
                && "23000".equals(sqlException.getSQLState())
                && sqlException.getErrorCode() == 1062;
    }

    private Throwable unwrapException(Exception exception) {
        return exception.getCause() == null ? exception : exception.getCause();
    }

    @FunctionalInterface
    private interface TechnicianListTask {
        List<Technician> loadTechnicians() throws Exception;
    }
}