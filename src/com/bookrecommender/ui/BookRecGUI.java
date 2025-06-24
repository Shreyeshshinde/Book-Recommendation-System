package com.bookrecommender.ui; // Assuming UI classes are in this sub-package

import com.bookrecommender.BookRecommendationSystem; // Need backend access
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

/**
 * Main application class for the Book Recommendation System GUI.
 * Sets up the main frame, panels, and handles application lifecycle.
 */
public class BookRecGUI {

    private JFrame mainFrame;
    private JPanel mainPanel; // Panel with CardLayout
    private CardLayout cardLayout;

    // Panel Names (Constants for CardLayout)
    public static final String LOGIN_PANEL = "LoginPanel";
    public static final String REGISTER_PANEL = "RegisterPanel";
    public static final String STUDENT_DASHBOARD_PANEL = "StudentDashboardPanel";
    public static final String ADMIN_DASHBOARD_PANEL = "AdminDashboardPanel";

    // Panels (We will create these classes later)
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private StudentDashboardPanel studentDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;

    /**
     * Constructor: Initializes the GUI components and structure.
     */
    public BookRecGUI() {
        prepareGUI();
    }

    /**
     * Initializes the main frame and panels.
     */
    private void prepareGUI() {
        mainFrame = new JFrame("Book Recommendation System");
        mainFrame.setSize(800, 600); // Initial size
        mainFrame.setLocationRelativeTo(null); // Center the window
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle close manually

        // Add a window listener to close the DB connection on exit
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ask for confirmation before closing
                int confirmed = JOptionPane.showConfirmDialog(mainFrame,
                        "Are you sure you want to exit?", "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (confirmed == JOptionPane.YES_OPTION) {
                    System.out.println("Exit confirmed. Closing database connection...");
                    BookRecommendationSystem.closeDatabase();
                    mainFrame.dispose(); // Close the window
                    System.exit(0); // Terminate the application
                }
                // If NO_OPTION, do nothing, window stays open
            }
        });


        // Setup CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Initialize Panels (Implement these classes next) ---
        loginPanel = new LoginPanel(this); // Pass reference to main GUI
        registerPanel = new RegisterPanel(this);
        studentDashboardPanel = new StudentDashboardPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        // --- Add Panels to CardLayout ---
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registerPanel, REGISTER_PANEL);
        mainPanel.add(studentDashboardPanel, STUDENT_DASHBOARD_PANEL);
        mainPanel.add(adminDashboardPanel, ADMIN_DASHBOARD_PANEL);
        // --- Add main panel to frame ---
        mainFrame.add(mainPanel);

        // Show the login panel first
        showPanel(LOGIN_PANEL);

        mainFrame.setVisible(true);
    }

    /**
     * Switches the visible panel using CardLayout.
     * @param panelName The name of the panel to show (use constants like LOGIN_PANEL).
     */
    public void showPanel(String panelName) {
         if (cardLayout != null && mainPanel != null) {
            System.out.println("Switching to panel: " + panelName); // Log panel switching
            cardLayout.show(mainPanel, panelName);
         } else {
             System.err.println("Error: CardLayout or mainPanel not initialized when trying to show " + panelName);
         }
    }

     /**
     * Provides access to the Student Dashboard panel instance.
     * Needed for panels to update other panels (e.g., after login).
     * @return The StudentDashboardPanel instance.
     */
     public StudentDashboardPanel getStudentDashboardPanel() {
         return studentDashboardPanel;
     }

     /**
      * Provides access to the Admin Dashboard panel instance.
      * @return The AdminDashboardPanel instance.
      */
     public AdminDashboardPanel getAdminDashboardPanel() {
         return adminDashboardPanel;
     }


    /**
     * Main method to launch the GUI application.
     */
    public static void main(String[] args) {
        // Set Look and Feel (Optional, for better appearance)
        try {
            // Use system look and feel for better native integration
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // // Or force a specific look and feel like Nimbus
            // for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            //     if ("Nimbus".equals(info.getName())) {
            //         UIManager.setLookAndFeel(info.getClassName());
            //         break;
            //     }
            // }
        } catch (Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
            // Application can continue with the default look and feel
        }


        // Run the GUI setup on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Connect to the database first
                System.out.println("Attempting to connect to database...");
                BookRecommendationSystem.connectDatabase();

                // Load book data into memory
                System.out.println("Attempting to load book data...");
                BookRecommendationSystem.loadBooks();

                // If connection and loading succeed, create and show the GUI
                System.out.println("Initialization complete. Starting GUI...");
                new BookRecGUI();

            } catch (SQLException e) {
                // Critical error during startup, show error and exit
                System.err.println("Failed to initialize application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Database connection or book loading failed. Application cannot start.\nError: " + e.getMessage(),
                        "Initialization Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Exit if DB connection fails
            } catch (Exception e) {
                 // Catch any other unexpected errors during startup
                 System.err.println("An unexpected error occurred during startup: " + e.getMessage());
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(null,
                         "An unexpected error occurred during startup.\nError: " + e.getMessage(),
                         "Startup Error", JOptionPane.ERROR_MESSAGE);
                 System.exit(1);
            }
        });
    }
}
