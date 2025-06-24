package com.bookrecommender.ui;

import com.bookrecommender.Admin;
import com.bookrecommender.BookRecommendationSystem;
import com.bookrecommender.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // For padding
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * JPanel for handling user login (Student or Admin) with a more modern look.
 */
public class LoginPanel extends JPanel implements ActionListener {

    private BookRecGUI mainGUI; // Reference to the main frame to switch panels

    // UI Components
    private JLabel userLabel, passLabel, roleLabel;
    private JTextField userText;
    private JPasswordField passText;
    private JComboBox<String> roleComboBox;
    private JButton loginButton, registerButton;

    // Define Colors for consistency
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Very light grey
    private static final Color FORM_BACKGROUND_COLOR = Color.WHITE;
    private static final Color PRIMARY_BUTTON_COLOR = new Color(0, 123, 255); // Bootstrap primary blue
    private static final Color SECONDARY_BUTTON_COLOR = new Color(108, 117, 125); // Bootstrap secondary grey
    private static final Color TEXT_COLOR = new Color(33, 37, 41); // Dark grey
    private static final Color BORDER_COLOR = new Color(222, 226, 230); // Light grey border

    // Define Fonts for consistency
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 28);


    public LoginPanel(BookRecGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new GridBagLayout()); // Use GridBagLayout to center the form panel
        setBackground(BACKGROUND_COLOR);

        // --- Central Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(FORM_BACKGROUND_COLOR);
        // Add padding inside the form panel and a subtle border
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), // Outer line border
                BorderFactory.createEmptyBorder(30, 40, 30, 40) // Inner padding
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Consistent padding around components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title Label
        JLabel titleLabel = new JLabel("Library Management Login", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across two columns
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 8, 25, 8); // More padding below title
        formPanel.add(titleLabel, gbc);

        // Reset grid width and bottom padding
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Username Row
        userLabel = new JLabel("Username:");
        userLabel.setFont(LABEL_FONT);
        userLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2; // Give label some weight
        formPanel.add(userLabel, gbc);

        userText = new JTextField(18); // Adjusted width
        userText.setFont(FIELD_FONT);
        userText.setMargin(new Insets(5, 8, 5, 8)); // Padding inside text field
        userText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8) // Match margin for consistency
        ));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8; // More weight for text field
        formPanel.add(userText, gbc);

        // Password Row
        passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        passLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        formPanel.add(passLabel, gbc);

        passText = new JPasswordField(18);
        passText.setFont(FIELD_FONT);
        passText.setMargin(new Insets(5, 8, 5, 8));
         passText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.8;
        formPanel.add(passText, gbc);

        // Role Row
        roleLabel = new JLabel("Login As:");
        roleLabel.setFont(LABEL_FONT);
        roleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        formPanel.add(roleLabel, gbc);

        String[] roles = {"Student", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(FIELD_FONT); // Use field font
        roleComboBox.setBackground(Color.WHITE); // Ensure background matches form
        // Basic border setting for combo box (LnF dependent)
        roleComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.8;
        formPanel.add(roleComboBox, gbc);

        // Button Row
        loginButton = new JButton("Login");
        loginButton.setFont(BUTTON_FONT);
        loginButton.setBackground(PRIMARY_BUTTON_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false); // Remove border for a flatter look
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(100, 40)); // Set preferred size
        loginButton.addActionListener(this);

        registerButton = new JButton("Register (Student)");
        registerButton.setFont(BUTTON_FONT);
        registerButton.setBackground(SECONDARY_BUTTON_COLOR);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setPreferredSize(new Dimension(180, 40));
        registerButton.addActionListener(this);

        // Use GridBagLayout for buttons too, spanning columns
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE; // Don't stretch buttons
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 8, 8, 8); // More top padding before buttons

        // Sub-panel for buttons to control spacing between them
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonContainer.setBackground(FORM_BACKGROUND_COLOR);
        buttonContainer.add(loginButton);
        buttonContainer.add(registerButton);
        formPanel.add(buttonContainer, gbc);


        // Add the formPanel to the main LoginPanel (this), centered
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1.0; // Allow horizontal centering
        mainGbc.weighty = 1.0; // Allow vertical centering
        mainGbc.anchor = GridBagConstraints.CENTER;
        mainGbc.fill = GridBagConstraints.NONE; // Don't resize form panel
        add(formPanel, mainGbc);


        // Add action listener to password field for Enter key press
        passText.addActionListener(this); // Trigger login on Enter in password field
    }

    /**
     * Handles button clicks (Login, Register) and Enter key press in password field.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton || e.getSource() == passText) {
            handleLogin();
        } else if (e.getSource() == registerButton) {
            // Clear fields before switching
            clearFields();
            mainGUI.showPanel(BookRecGUI.REGISTER_PANEL);
        }
    }

    /**
     * Processes the login attempt.
     */
    private void handleLogin() {
        String username = userText.getText();
        String password = new String(passText.getPassword());
        String selectedRole = (String) roleComboBox.getSelectedItem();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show loading indicator (optional, basic)
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false); // Disable button during processing

        // Use SwingWorker for database operations to avoid freezing UI
        SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                Connection conn = BookRecommendationSystem.getConnection(); // Get connection
                if ("Student".equals(selectedRole)) {
                    return Student.login(conn, username, password);
                } else if ("Admin".equals(selectedRole)) {
                    return Admin.login(conn, username, password);
                }
                return null; // Should not happen
            }

            @Override
            protected void done() {
                try {
                    Object result = get(); // Get result from doInBackground
                    if (result instanceof Student) {
                        Student student = (Student) result;
                        JOptionPane.showMessageDialog(LoginPanel.this, "Student Login Successful! Welcome " + student.getName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                        mainGUI.getStudentDashboardPanel().setStudent(student); // Update dashboard
                        clearFields();
                        mainGUI.showPanel(BookRecGUI.STUDENT_DASHBOARD_PANEL);
                    } else if (result instanceof Admin) {
                        Admin admin = (Admin) result;
                         JOptionPane.showMessageDialog(LoginPanel.this, "Admin Login Successful! Welcome " + admin.getName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                         mainGUI.getAdminDashboardPanel().setAdmin(admin); // Update dashboard
                         clearFields();
                         mainGUI.showPanel(BookRecGUI.ADMIN_DASHBOARD_PANEL);
                    } else {
                        // Login failed (result was null)
                         if ("Student".equals(selectedRole)) {
                            JOptionPane.showMessageDialog(LoginPanel.this, "Invalid Student username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                         } else {
                            JOptionPane.showMessageDialog(LoginPanel.this, "Invalid Admin username or password, or user is not an admin.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                         }
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause(); // Get the actual exception from SwingWorker
                     if (cause instanceof SQLException) {
                         JOptionPane.showMessageDialog(LoginPanel.this, "Database error during login: " + cause.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                     } else {
                         JOptionPane.showMessageDialog(LoginPanel.this, "An unexpected error occurred during login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                     }
                    ex.printStackTrace(); // Log detailed error
                } finally {
                    // Restore UI state
                    setCursor(Cursor.getDefaultCursor());
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute(); // Start the worker thread
    }

    /**
     * Clears the input fields.
     */
    public void clearFields() {
        userText.setText("");
        passText.setText("");
        roleComboBox.setSelectedIndex(0); // Default to Student
    }
}
