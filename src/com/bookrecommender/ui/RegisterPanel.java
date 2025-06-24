package com.bookrecommender.ui;

import com.bookrecommender.BookRecommendationSystem;
import com.bookrecommender.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // For padding
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException; // Import for exception handling

/**
 * JPanel for handling new student registration with a more modern look.
 */
public class RegisterPanel extends JPanel implements ActionListener {

    private BookRecGUI mainGUI; // Reference to the main frame

    // UI Components
    private JLabel userLabel, passLabel, nameLabel, emailLabel;
    private JTextField userText, nameText, emailText;
    private JPasswordField passText;
    private JButton registerButton, backButton;

    // Define Colors (Consistent with LoginPanel)
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Very light grey
    private static final Color FORM_BACKGROUND_COLOR = Color.WHITE;
    private static final Color PRIMARY_BUTTON_COLOR = new Color(40, 167, 69); // Bootstrap success green
    private static final Color SECONDARY_BUTTON_COLOR = new Color(108, 117, 125); // Bootstrap secondary grey
    private static final Color TEXT_COLOR = new Color(33, 37, 41); // Dark grey
    private static final Color BORDER_COLOR = new Color(222, 226, 230); // Light grey border

    // Define Fonts (Consistent with LoginPanel)
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 28);


    public RegisterPanel(BookRecGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new GridBagLayout()); // Use GridBagLayout to center the form panel
        setBackground(BACKGROUND_COLOR);

        // --- Central Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(FORM_BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title Label
        JLabel titleLabel = new JLabel("Register New Student", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 8, 25, 8);
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
        gbc.weightx = 0.2;
        formPanel.add(userLabel, gbc);

        userText = new JTextField(18);
        userText.setFont(FIELD_FONT);
        userText.setMargin(new Insets(5, 8, 5, 8));
        userText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
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

        // Name Row
        nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(LABEL_FONT);
        nameLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        formPanel.add(nameLabel, gbc);

        nameText = new JTextField(18);
        nameText.setFont(FIELD_FONT);
        nameText.setMargin(new Insets(5, 8, 5, 8));
        nameText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.8;
        formPanel.add(nameText, gbc);

        // Email Row
        emailLabel = new JLabel("Email:");
        emailLabel.setFont(LABEL_FONT);
        emailLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.2;
        formPanel.add(emailLabel, gbc);

        emailText = new JTextField(18);
        emailText.setFont(FIELD_FONT);
        emailText.setMargin(new Insets(5, 8, 5, 8));
        emailText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 0.8;
        formPanel.add(emailText, gbc);

        // Button Row
        registerButton = new JButton("Register");
        registerButton.setFont(BUTTON_FONT);
        registerButton.setBackground(PRIMARY_BUTTON_COLOR);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.addActionListener(this);

        backButton = new JButton("Back to Login");
        backButton.setFont(BUTTON_FONT);
        backButton.setBackground(SECONDARY_BUTTON_COLOR);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.addActionListener(this);

        // Button Container
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonContainer.setBackground(FORM_BACKGROUND_COLOR);
        buttonContainer.add(registerButton);
        buttonContainer.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 8, 8, 8);
        formPanel.add(buttonContainer, gbc);

        // Add formPanel to the main RegisterPanel (this), centered
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1.0;
        mainGbc.weighty = 1.0;
        mainGbc.anchor = GridBagConstraints.CENTER;
        mainGbc.fill = GridBagConstraints.NONE;
        add(formPanel, mainGbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            handleRegistration();
        } else if (e.getSource() == backButton) {
            clearFields();
            mainGUI.showPanel(BookRecGUI.LOGIN_PANEL); // Go back to login
        }
    }

    /**
     * Processes the registration attempt using SwingWorker.
     */
    private void handleRegistration() {
        String username = userText.getText();
        String password = new String(passText.getPassword());
        String name = nameText.getText();
        String email = emailText.getText();

        // Basic check - more detailed validation is in Student.registerStudent
        if (username.trim().isEmpty() || password.isEmpty() || name.trim().isEmpty() || email.trim().isEmpty()) {
             JOptionPane.showMessageDialog(this, "All fields are required.", "Registration Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // Disable button and show wait cursor
        registerButton.setEnabled(false);
        backButton.setEnabled(false); // Also disable back button during process
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                Connection conn = BookRecommendationSystem.getConnection();
                // Call the backend method which includes validation
                return Student.registerStudent(conn, username, password, name, email);
            }

            @Override
            protected void done() {
                try {
                    String resultMessage = get(); // Get result from backend
                    if (resultMessage == null) {
                        // Handle unexpected null result
                         JOptionPane.showMessageDialog(RegisterPanel.this, "An unexpected error occurred (null result).", "Error", JOptionPane.ERROR_MESSAGE);
                         return; // Exit finally block below will re-enable buttons
                    }

                    // Trim the message before checking content
                    String trimmedMessage = resultMessage.trim();

                    // **** MODIFIED CHECK ****
                    // Check if the message starts with the ACTUAL success message prefix
                    if (trimmedMessage.startsWith("Registration successful!")) {
                        JOptionPane.showMessageDialog(RegisterPanel.this, trimmedMessage, "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                        clearFields();
                        mainGUI.showPanel(BookRecGUI.LOGIN_PANEL); // Go back to login after success
                    } else if (trimmedMessage.startsWith("Warning:")) { // Keep warning check
                         JOptionPane.showMessageDialog(RegisterPanel.this, trimmedMessage, "Registration Warning", JOptionPane.WARNING_MESSAGE);
                    } else { // Assume other messages are errors (e.g., start with "Error:", "Registration Error:")
                        JOptionPane.showMessageDialog(RegisterPanel.this, trimmedMessage, "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) { // Catch exceptions from SwingWorker
                     Throwable cause = ex.getCause();
                     if (cause instanceof SQLException) {
                         JOptionPane.showMessageDialog(RegisterPanel.this, "Database error during registration: " + cause.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                     } else {
                         JOptionPane.showMessageDialog(RegisterPanel.this, "An unexpected error occurred during registration: " + (cause != null ? cause.getMessage() : ex.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                     }
                    ex.printStackTrace();
                } finally {
                    // Re-enable buttons and restore cursor
                    registerButton.setEnabled(true);
                    backButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

     /**
     * Clears the input fields.
     */
    public void clearFields() {
        userText.setText("");
        passText.setText("");
        nameText.setText("");
        emailText.setText("");
    }
}
