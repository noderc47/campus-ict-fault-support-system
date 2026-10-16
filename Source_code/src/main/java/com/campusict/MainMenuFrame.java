package com.campusict;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The application's main navigation window.
 */
public class MainMenuFrame extends JFrame {

    private static final Color PRIMARY_COLOR = new Color(24, 78, 119);

    public MainMenuFrame() {
        setTitle("Campus ICT Fault Reporting and Support System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 430));
        setSize(620, 480);
        setLocationRelativeTo(null);

        createUserInterface();
    }

    private void createUserInterface() {
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Campus ICT Support System", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel instructionLabel = new JLabel("Select an option to continue", SwingConstants.CENTER);
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(instructionLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 0, 12));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createUsersManagementButton());
        buttonPanel.add(createTechniciansManagementButton());
        buttonPanel.add(createReportFaultButton());
        buttonPanel.add(createSearchManageTicketsButton());
        buttonPanel.add(createExitButton());

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);
        setContentPane(contentPanel);
    }

    private JButton createMenuButton(String buttonText, String formName) {
        JButton button = new JButton(buttonText);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        button.addActionListener(event -> showNotImplementedMessage(formName));
        return button;
    }

    private JButton createUsersManagementButton() {
        JButton button = new JButton("Users Management");
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        button.addActionListener(event -> openUsersManagement());
        return button;
    }

    private void openUsersManagement() {
        setVisible(false);
        new UsersManagementFrame(this).setVisible(true);
    }

    private JButton createTechniciansManagementButton() {
        JButton button = new JButton("Technicians Management");
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        button.addActionListener(event -> openTechniciansManagement());
        return button;
    }

    private void openTechniciansManagement() {
        setVisible(false);
        new TechniciansManagementFrame(this).setVisible(true);
    }

    private JButton createReportFaultButton() {
        JButton button = new JButton("Report Fault");
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        button.addActionListener(event -> openReportFault());
        return button;
    }

    private void openReportFault() {
        setVisible(false);
        new ReportFaultFrame(this).setVisible(true);
    }

    private JButton createSearchManageTicketsButton() {
        JButton button = new JButton("Search & Manage Tickets");
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        button.addActionListener(event -> openSearchManageTickets());
        return button;
    }

    private void openSearchManageTickets() {
        setVisible(false);
        new SearchManageTicketsFrame(this).setVisible(true);
    }

    private JButton createExitButton() {
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        exitButton.addActionListener(event -> System.exit(0));
        return exitButton;
    }

    private void showNotImplementedMessage(String formName) {
        JOptionPane.showMessageDialog(
                this,
                formName + " is not implemented yet.",
                "Not implemented yet",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}