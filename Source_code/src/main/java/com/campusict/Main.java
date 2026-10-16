package com.campusict;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starts the Campus ICT Fault Reporting and Support System.
 */
public final class Main {

    private Main() {
        // Prevents this utility class from being instantiated.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new MainMenuFrame().setVisible(true);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // The standard Swing appearance is used if the system style is unavailable.
        }
    }
}