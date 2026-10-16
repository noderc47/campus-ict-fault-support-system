package com.campusict;

import com.campusict.dao.UserDAO;
import com.campusict.model.User;
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
 * Provides the Swing interface for creating and managing users.
 */
public class UsersManagementFrame extends JFrame {

    private static final int FULL_NAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final MainMenuFrame mainMenuFrame;
    private final UserDAO userDAO = new UserDAO();
    private final JTextField userIdField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Student", "Staff"});
    private final JTextField searchField = new JTextField(24);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"User ID", "Full Name", "Email", "Phone", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable usersTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");
    private final JButton addButton = new JButton("Add");
    private final JButton searchButton = new JButton("Search");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton backButton = new JButton("Back to Main Menu");

    /**
     * Builds the Users Management window and loads users in the background.
     *
     * @param mainMenuFrame existing menu window to show again when this form closes
     */
    public UsersManagementFrame(MainMenuFrame mainMenuFrame) {
        this.mainMenuFrame = mainMenuFrame;
        setTitle("Users Management - Campus ICT Support System");
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
        loadUsers();
    }

    private void createUserInterface() {
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Users Management", SwingConstants.CENTER);
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
        detailsPanel.setBorder(BorderFactory.createTitledBorder("User Details"));
        userIdField.setEditable(false);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(detailsPanel, constraints, 0, "User ID:", userIdField);
        addFormRow(detailsPanel, constraints, 1, "Full Name:*", fullNameField);
        addFormRow(detailsPanel, constraints, 2, "Email:*", emailField);
        addFormRow(detailsPanel, constraints, 3, "Phone:", phoneField);
        addFormRow(detailsPanel, constraints, 4, "Role:*", roleComboBox);
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
        tablePanel.setBorder(BorderFactory.createTitledBorder("Users"));
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setAutoCreateRowSorter(true);
        usersTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedUserIntoFields();
            }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search ID, name, or email:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchField.addActionListener(event -> searchUsers());

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
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

        addButton.addActionListener(event -> addUser());
        searchButton.addActionListener(event -> searchUsers());
        updateButton.addActionListener(event -> updateUser());
        deleteButton.addActionListener(event -> deleteUser());
        clearButton.addActionListener(event -> clearForm());
        refreshButton.addActionListener(event -> loadUsers());
        backButton.addActionListener(event -> returnToMainMenu());

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(actionsPanel, BorderLayout.CENTER);
        return footerPanel;
    }

    private void loadUsers() {
        runUserListWorker("Loading users...", userDAO::findAll);
    }

    private void searchUsers() {
        runUserListWorker("Searching users...", () -> userDAO.search(searchField.getText()));
    }

    private void runUserListWorker(String loadingMessage, UserListTask task) {
        setBusy(true, loadingMessage);
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return task.loadUsers();
            }

            @Override
            protected void done() {
                try {
                    displayUsers(get());
                    statusLabel.setText("Showing " + tableModel.getRowCount() + " user(s).");
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                } finally {
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private void addUser() {
        User user = createValidatedUser();
        if (user == null) {
            return;
        }

        setBusy(true, "Adding user...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return userDAO.insert(user);
            }

            @Override
            protected void done() {
                try {
                    int userId = get();
                    clearForm();
                    JOptionPane.showMessageDialog(UsersManagementFrame.this,
                            "User added successfully with User ID " + userId + ".",
                            "User Added", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private void updateUser() {
        Integer selectedUserId = getSelectedUserId();
        if (selectedUserId == null) {
            showSelectionMessage("Select a user row before updating.");
            return;
        }

        User user = createValidatedUser();
        if (user == null) {
            return;
        }
        user.setUserId(selectedUserId);

        setBusy(true, "Updating user...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.update(user);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(UsersManagementFrame.this,
                                "User updated successfully.", "User Updated", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadUsers();
                    } else {
                        showDatabaseError(new SQLException("The selected user no longer exists."));
                        setBusy(false, "Ready");
                    }
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private void deleteUser() {
        Integer selectedUserId = getSelectedUserIdFromTable();
        if (selectedUserId == null) {
            showSelectionMessage("Please select a record to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the selected user (User ID " + selectedUserId + ")?\n"
                        + "Users with linked tickets cannot be deleted because ticket reporter links are restricted.\n"
                        + "This cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            statusLabel.setText("Deletion cancelled. No records were changed.");
            return;
        }

        setBusy(true, "Deleting user...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.delete(selectedUserId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(UsersManagementFrame.this,
                                "User deleted successfully.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadUsers();
                    } else {
                        showMissingUserMessage();
                        clearForm();
                        loadUsers();
                    }
                } catch (Exception exception) {
                    showDatabaseError(unwrapException(exception));
                    setBusy(false, "Ready");
                }
            }
        }.execute();
    }

    private User createValidatedUser() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (fullName.isEmpty()) {
            showValidationMessage("Full name is required.");
            fullNameField.requestFocusInWindow();
            return null;
        }
        if (fullName.length() > FULL_NAME_MAX_LENGTH) {
            showValidationMessage("Full name must be " + FULL_NAME_MAX_LENGTH + " characters or fewer.");
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
        if (phone.length() > PHONE_MAX_LENGTH) {
            showValidationMessage("Phone must be " + PHONE_MAX_LENGTH + " characters or fewer.");
            return null;
        }
        if (!"Student".equals(role) && !"Staff".equals(role)) {
            showValidationMessage("Select Student or Staff for the role.");
            return null;
        }
        return new User(null, fullName, email, phone, role);
    }

    private void loadSelectedUserIntoFields() {
        int selectedViewRow = usersTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return;
        }
        int selectedModelRow = usersTable.convertRowIndexToModel(selectedViewRow);
        userIdField.setText(tableModel.getValueAt(selectedModelRow, 0).toString());
        fullNameField.setText(tableModel.getValueAt(selectedModelRow, 1).toString());
        emailField.setText(tableModel.getValueAt(selectedModelRow, 2).toString());
        Object phone = tableModel.getValueAt(selectedModelRow, 3);
        phoneField.setText(phone == null ? "" : phone.toString());
        roleComboBox.setSelectedItem(tableModel.getValueAt(selectedModelRow, 4).toString());
        statusLabel.setText("Selected User ID " + userIdField.getText() + ".");
    }

    private Integer getSelectedUserId() {
        if (userIdField.getText().isBlank()) {
            return null;
        }
        return Integer.valueOf(userIdField.getText());
    }

    private Integer getSelectedUserIdFromTable() {
        int selectedViewRow = usersTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return null;
        }
        int selectedModelRow = usersTable.convertRowIndexToModel(selectedViewRow);
        return (Integer) tableModel.getValueAt(selectedModelRow, 0);
    }

    private void displayUsers(List<User> users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getUserId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole()
            });
        }
    }

    private void clearForm() {
        usersTable.clearSelection();
        userIdField.setText("");
        fullNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        roleComboBox.setSelectedItem("Student");
        statusLabel.setText("Form cleared. No records were changed.");
        fullNameField.requestFocusInWindow();
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
        JOptionPane.showMessageDialog(this, message, "No User Selected", JOptionPane.WARNING_MESSAGE);
    }

    private void showMissingUserMessage() {
        JOptionPane.showMessageDialog(this,
                "The selected user no longer exists. User records will be reloaded.",
                "User No Longer Available", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(Throwable exception) {
        if (isDuplicateEmailError(exception)) {
            JOptionPane.showMessageDialog(this, "That email address is already assigned to another user.",
                    "Duplicate Email", JOptionPane.WARNING_MESSAGE);
        } else if (isLinkedTicketError(exception)) {
            JOptionPane.showMessageDialog(this,
                    "This user cannot be deleted because they have linked tickets. "
                            + "Keep the user record or reassign/remove the linked tickets first.",
                    "User Has Linked Tickets", JOptionPane.WARNING_MESSAGE);
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

    private boolean isLinkedTicketError(Throwable exception) {
        return exception instanceof SQLException sqlException
                && "23000".equals(sqlException.getSQLState())
                && sqlException.getErrorCode() == 1451;
    }

    private Throwable unwrapException(Exception exception) {
        return exception.getCause() == null ? exception : exception.getCause();
    }

    @FunctionalInterface
    private interface UserListTask {
        List<User> loadUsers() throws Exception;
    }
}