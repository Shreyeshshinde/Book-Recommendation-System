package com.bookrecommender.ui;

import com.bookrecommender.Admin;
import com.bookrecommender.BookRecommendationSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map; // Keep this import
import java.util.concurrent.ExecutionException;


/**
 * JPanel for the Admin Dashboard with a more modern look.
 */
public class AdminDashboardPanel extends JPanel implements ActionListener {

    private BookRecGUI mainGUI;
    private Admin currentAdmin;

    // UI Components
    private JLabel welcomeLabel;
    private JButton logoutButton;

    // Input Area Components
    private JPanel inputPanel;
    private JTextField studentUsernameField, bookIdField, fineStudentUsernameField;

    // Action Buttons
    private JButton issueBookButton, calculateFineButton, viewIssuedButton, addBookButton, viewStudentsButton;

    // Results Display Area
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JLabel statusLabel; // Status label

    // Define Colors (Consistent Theme)
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color HEADER_BACKGROUND_COLOR = new Color(233, 236, 239);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 58, 64);
    private static final Color INPUT_AREA_BG_COLOR = new Color(241, 243, 245); // Slightly different grey for input area
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color BUTTON_DANGER_COLOR = new Color(220, 20, 60);

    // Define Fonts (Consistent Theme)
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13); // Slightly smaller for admin panel
    private static final Font FIELD_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font WELCOME_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12); // Slightly smaller buttons
    private static final Font TABLE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font TABLE_HEADER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font STATUS_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 12);
    private static final Font INPUT_TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);


    // Table Column Headers
    private final String[] allIssuedBooksColumns = {"Issue ID", "Student", "Book Title", "Book ID", "Issued", "Due", "Status", "Fine ($)"};
    private final String[] allStudentsColumns = {"User ID", "Username", "Name", "Email"};

    public AdminDashboardPanel(BookRecGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        setBackground(BACKGROUND_COLOR);

        // --- Top Panel (Header) ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(true);
        topPanel.setBackground(HEADER_BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        welcomeLabel = new JLabel("Welcome, Admin!", JLabel.LEFT);
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setForeground(TEXT_COLOR);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        logoutButton = createStyledButton("Logout", BUTTON_DANGER_COLOR, 90); // Fixed width logout
        logoutButton.addActionListener(this);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Center: Results Table ---
        tableModel = new DefaultTableModel() {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(TABLE_FONT);
        resultsTable.setRowHeight(24);
        resultsTable.setGridColor(BORDER_COLOR);
        resultsTable.setShowGrid(true);
        resultsTable.setIntercellSpacing(new Dimension(0, 1));
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setBackground(Color.WHITE);
        resultsTable.setForeground(TEXT_COLOR);
        resultsTable.setSelectionBackground(new Color(0, 123, 255).brighter());
        resultsTable.setSelectionForeground(Color.WHITE);

        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // --- Left Panel: Actions and Inputs ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(true); // Make opaque to set background
        leftPanel.setBackground(INPUT_AREA_BG_COLOR); // Different background for this area
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), // Outer border
                BorderFactory.createEmptyBorder(15, 10, 15, 10) // Inner padding
        ));

        // View Actions (Grouped at top)
        JLabel viewLabel = new JLabel("View Data");
        viewLabel.setFont(INPUT_TITLE_FONT);
        viewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(viewLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        viewIssuedButton = createStyledButton("View All Issued Books", new Color(23, 162, 184)); // Info Blue
        viewStudentsButton = createStyledButton("View All Students", new Color(108, 117, 125)); // Secondary Grey
        leftPanel.add(viewIssuedButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(viewStudentsButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20))); // More space

        // Input sections
        JLabel actionsLabel = new JLabel("Perform Actions");
        actionsLabel.setFont(INPUT_TITLE_FONT);
        actionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(actionsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Issue Book Sub-Panel ---
        JPanel issuePanel = createInputSectionPanel("Issue Book");
        issuePanel.add(new JLabel("Student User:"), createGBC(0,0, GridBagConstraints.EAST));
        studentUsernameField = createStyledTextField(10);
        issuePanel.add(studentUsernameField, createGBC(1,0, GridBagConstraints.WEST));
        issuePanel.add(new JLabel("Book ID:"), createGBC(0,1, GridBagConstraints.EAST));
        bookIdField = createStyledTextField(5);
        issuePanel.add(bookIdField, createGBC(1,1, GridBagConstraints.WEST));
        issueBookButton = createStyledButton("Issue", new Color(40, 167, 69)); // Green
        issuePanel.add(issueBookButton, createGBC(0,2, GridBagConstraints.CENTER, 2)); // Span 2 cols, center
        leftPanel.add(issuePanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Calculate Fine Sub-Panel ---
        JPanel finePanel = createInputSectionPanel("Calculate Fine");
        finePanel.add(new JLabel("Student User:"), createGBC(0,0, GridBagConstraints.EAST));
        fineStudentUsernameField = createStyledTextField(10);
        finePanel.add(fineStudentUsernameField, createGBC(1,0, GridBagConstraints.WEST));
        calculateFineButton = createStyledButton("Calculate Fine", new Color(255, 193, 7)); // Warning Yellow
        finePanel.add(calculateFineButton, createGBC(0,1, GridBagConstraints.CENTER, 2));
        leftPanel.add(finePanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Add Book Button ---
        addBookButton = createStyledButton("Add New Book...", new Color(102, 16, 242)); // Indigo
        leftPanel.add(addBookButton);

        leftPanel.add(Box.createVerticalGlue()); // Pushes components up

        add(leftPanel, BorderLayout.WEST);

         // --- Status Label (optional, could go in footer) ---
         statusLabel = new JLabel("Ready", JLabel.CENTER);
         statusLabel.setFont(STATUS_FONT);
         statusLabel.setForeground(Color.GRAY);
         statusLabel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
         add(statusLabel, BorderLayout.SOUTH); // Add status label at the bottom
    }

     // Helper for GridBagConstraints
    private GridBagConstraints createGBC(int x, int y, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.anchor = anchor;
        gbc.insets = new Insets(3, 5, 3, 5); // Consistent insets
        return gbc;
    }
     // Overload for spanning columns
     private GridBagConstraints createGBC(int x, int y, int anchor, int gridwidth) {
        GridBagConstraints gbc = createGBC(x, y, anchor);
        gbc.gridwidth = gridwidth;
        if(anchor == GridBagConstraints.CENTER) gbc.fill = GridBagConstraints.NONE; // Don't fill if centered
        return gbc;
    }


    // Helper to create styled input text fields
    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(FIELD_FONT);
        textField.setMargin(new Insets(4, 6, 4, 6));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return textField;
    }

    // Helper to create input section panels
    private JPanel createInputSectionPanel(String title) {
         JPanel panel = new JPanel(new GridBagLayout());
         panel.setBackground(INPUT_AREA_BG_COLOR); // Match left panel bg
         // Subtle border, maybe TitledBorder if preferred
         panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), // Top border separator
            BorderFactory.createEmptyBorder(10, 5, 10, 5) // Padding
         ));
         // panel.setBorder(BorderFactory.createTitledBorder(title)); // Alternative border
         panel.setAlignmentX(Component.CENTER_ALIGNMENT);
         // Constrain width
         panel.setMaximumSize(new Dimension(220, 200)); // Limit panel width
         panel.setPreferredSize(new Dimension(220, 150)); // Preferred size
         return panel;
    }


    // Helper to create styled buttons - MODIFIED for Admin Panel
    private JButton createStyledButton(String text, Color bgColor) {
       return createStyledButton(text, bgColor, (int)(text.length() * 8 + 40)); // Auto-width estimate
    }
     private JButton createStyledButton(String text, Color bgColor, int preferredWidth) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension size = new Dimension(preferredWidth, 30); // Height 30
        button.setPreferredSize(size);
        button.setMaximumSize(size); // Limit size in BoxLayout
        button.addActionListener(this); // Add listener here
        return button;
    }

    /**
     * Sets the currently logged-in admin and updates the welcome message.
     * @param admin The logged-in Admin object.
     */
    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        if (admin != null) {
            welcomeLabel.setText("Welcome, Admin " + admin.getName() + " (ID: " + admin.getUserId() + ")");
            clearTable();
            clearInputFields();
            statusLabel.setText("Ready");
        } else {
            welcomeLabel.setText("Welcome, Admin!");
            clearTable();
            statusLabel.setText("Logged out");
        }
    }

     /** Clears the results table. */
    private void clearTable() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
         statusLabel.setText("Table cleared");
    }

    /** Clears the input fields in the input panel. */
    private void clearInputFields() {
         studentUsernameField.setText("");
         bookIdField.setText("");
         fineStudentUsernameField.setText("");
    }

    /** Updates the JTable with new data and column headers. */
    private void updateTable(List<String[]> data, String[] columnNames) {
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0); // Clear existing rows
        if (data != null) {
            for (String[] row : data) {
                tableModel.addRow(row);
            }
        }
         statusLabel.setText("Displayed " + (data != null ? data.size() : 0) + " items.");
    }

     // Disable all action buttons during background task
    private void setActionsEnabled(boolean enabled) {
        viewIssuedButton.setEnabled(enabled);
        viewStudentsButton.setEnabled(enabled);
        issueBookButton.setEnabled(enabled);
        calculateFineButton.setEnabled(enabled);
        addBookButton.setEnabled(enabled);
        logoutButton.setEnabled(enabled); // Also disable logout during action
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentAdmin == null && e.getSource() != logoutButton) {
            JOptionPane.showMessageDialog(this, "Error: No admin is logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object source = e.getSource();
        setActionsEnabled(false); // Disable buttons
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Show wait cursor

        // --- Handle View Actions ---
        if (source == viewIssuedButton) {
            statusLabel.setText("Loading issued books...");
            executeBackgroundTask(this::loadAllIssuedBooks, "loading issued books");
        } else if (source == viewStudentsButton) {
             statusLabel.setText("Loading students...");
            executeBackgroundTask(this::loadAllStudents, "loading students");
        }
        // --- Handle Input Actions ---
        else if (source == issueBookButton) {
            statusLabel.setText("Issuing book...");
            executeBackgroundTask(this::handleIssueBookAction, "issuing book");
        } else if (source == calculateFineButton) {
             statusLabel.setText("Calculating fines...");
            executeBackgroundTask(this::handleCalculateFineAction, "calculating fines");
        } else if (source == addBookButton) {
             statusLabel.setText("Ready to add book...");
             // Dialog is modal, doesn't need background task here unless DB check is slow
             handleAddBookDialog();
             setActionsEnabled(true); // Re-enable after modal dialog
             setCursor(Cursor.getDefaultCursor());
        }
        // --- Handle Logout ---
        else if (source == logoutButton) {
            int confirmed = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirmed == JOptionPane.YES_OPTION) {
                setAdmin(null); // Clear current admin
                mainGUI.showPanel(BookRecGUI.LOGIN_PANEL); // Go back to login
            }
             // Re-enable buttons/cursor if logout cancelled or finished
             setActionsEnabled(true);
             setCursor(Cursor.getDefaultCursor());
        } else {
             // If action wasn't handled, re-enable buttons/cursor
             setActionsEnabled(true);
             setCursor(Cursor.getDefaultCursor());
        }
    }

    // --- Background Task Execution Helper ---
    // Functional interface for tasks that return a result (e.g., List<String[]>, String)
    @FunctionalInterface
    private interface BackgroundTask<T> {
        T execute() throws Exception;
    }

    // Generic method to execute tasks in background
    private <T> void executeBackgroundTask(BackgroundTask<T> task, String actionDescription) {
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    // Process result based on type (could be List or String)
                    if (result instanceof List) {
                        // Assume it's table data, but need columns
                        // This approach is slightly flawed, better to handle updates specifically
                        // For now, let's assume specific methods update table directly if needed
                        statusLabel.setText(actionDescription + " complete.");
                    } else if (result instanceof String) {
                        // Assume it's a status message from issue/fine/add
                         if (((String) result).startsWith("Success")) {
                            JOptionPane.showMessageDialog(AdminDashboardPanel.this, result, "Success", JOptionPane.INFORMATION_MESSAGE);
                         } else if (((String) result).startsWith("Warning:")) {
                             JOptionPane.showMessageDialog(AdminDashboardPanel.this, result, "Warning", JOptionPane.WARNING_MESSAGE);
                         } else if (((String) result).contains("Fine Calculation Result")){ // Special handling for fine result
                              JTextArea textArea = new JTextArea((String) result);
                              textArea.setWrapStyleWord(true);
                              textArea.setLineWrap(true);
                              textArea.setEditable(false);
                              textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                              JScrollPane textScrollPane = new JScrollPane(textArea);
                              textScrollPane.setPreferredSize(new Dimension(450, 250));
                              JOptionPane.showMessageDialog(AdminDashboardPanel.this, textScrollPane, "Fine Calculation Result", JOptionPane.INFORMATION_MESSAGE);
                         }
                         else {
                            JOptionPane.showMessageDialog(AdminDashboardPanel.this, result, "Operation Failed", JOptionPane.ERROR_MESSAGE);
                         }
                    }
                     // Potentially refresh relevant view after success?
                     // E.g., after issuing book, refresh 'View All Issued Books'
                     // Consider adding a refresh button or auto-refresh logic

                } catch (Exception ex) {
                    handleWorkerException(ex, actionDescription);
                } finally {
                    setActionsEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }


    // --- Specific Action Methods for Background Tasks ---

    private List<String[]> loadAllIssuedBooks() throws SQLException {
         Connection conn = BookRecommendationSystem.getConnection();
         List<String[]> data = Admin.getAllIssuedBooks(conn);
         // Update table directly here is tricky due to threading, update in done()
         SwingUtilities.invokeLater(() -> { // Ensure UI update is on EDT
             updateTable(data, allIssuedBooksColumns);
             if(data.isEmpty()) JOptionPane.showMessageDialog(this, "No books are currently issued.", "Info", JOptionPane.INFORMATION_MESSAGE);
         });
         return data; // Return data for potential use in done() if needed
    }

     private List<String[]> loadAllStudents() throws SQLException {
         Connection conn = BookRecommendationSystem.getConnection();
         List<String[]> data = Admin.getAllStudents(conn);
         SwingUtilities.invokeLater(() -> { // Ensure UI update is on EDT
             updateTable(data, allStudentsColumns);
             if(data.isEmpty()) JOptionPane.showMessageDialog(this, "No students found.", "Info", JOptionPane.INFORMATION_MESSAGE);
         });
         return data;
    }


    private String handleIssueBookAction() throws SQLException {
         String studentUsername = studentUsernameField.getText();
         String bookIdStr = bookIdField.getText();
         if (studentUsername.trim().isEmpty() || bookIdStr.trim().isEmpty()) {
             return "Input Error: Student Username and Book ID are required."; // Return error string
         }
         try {
             int bookId = Integer.parseInt(bookIdStr.trim());
             Connection conn = BookRecommendationSystem.getConnection();
             String result = Admin.issueBookToStudent(conn, studentUsername, bookId);
             if (result.startsWith("Success")) {
                 // Clear fields on success (must be done on EDT)
                 SwingUtilities.invokeLater(() -> {
                     studentUsernameField.setText("");
                     bookIdField.setText("");
                 });
             }
             return result; // Return result string
         } catch (NumberFormatException ex) {
             return "Input Error: Invalid Book ID format. Please enter a number."; // Return error string
         }
         // SQLException will be caught by SwingWorker's done() method
    }

    private String handleCalculateFineAction() throws SQLException {
        String studentUsername = fineStudentUsernameField.getText();
         if (studentUsername.trim().isEmpty()) {
             return "Input Error: Student Username is required.";
         }
         Connection conn = BookRecommendationSystem.getConnection();
         // This method already returns a detailed string
         return Admin.calculateFineForStudent(conn, studentUsername);
         // SQLException will be caught by SwingWorker's done() method
    }

    // Dialog for adding book (runs on EDT, DB part could be background task if slow)
    private void handleAddBookDialog() {
        JTextField titleField = createStyledTextField(20); // Use styled fields
        JTextField authorField = createStyledTextField(20);
        JTextField genreField = createStyledTextField(20);
        JTextField yearField = createStyledTextField(5);
        JTextField copiesField = createStyledTextField(5);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10)); // Grid layout, more spacing
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Padding
        panel.add(new JLabel("Title:")); panel.add(titleField);
        panel.add(new JLabel("Author:")); panel.add(authorField);
        panel.add(new JLabel("Genre:")); panel.add(genreField);
        panel.add(new JLabel("Pub. Year:")); panel.add(yearField);
        panel.add(new JLabel("Total Copies:")); panel.add(copiesField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Perform the add book action (could use SwingWorker if DB check/insert is slow)
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setActionsEnabled(false);
            try {
                String title = titleField.getText();
                String author = authorField.getText();
                String genre = genreField.getText();
                 if (title.trim().isEmpty() || author.trim().isEmpty() || genre.trim().isEmpty() ||
                     yearField.getText().trim().isEmpty() || copiesField.getText().trim().isEmpty()) {
                     JOptionPane.showMessageDialog(this, "All fields are required to add a book.", "Input Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                int year = Integer.parseInt(yearField.getText().trim());
                int copies = Integer.parseInt(copiesField.getText().trim());

                Connection conn = BookRecommendationSystem.getConnection();
                String addResult = Admin.addNewBook(conn, title, author, genre, year, copies);

                if (addResult.startsWith("Success")) {
                    JOptionPane.showMessageDialog(this, addResult, "Add Book Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     if (addResult.startsWith("Warning:")) {
                         JOptionPane.showMessageDialog(this, addResult, "Add Book Warning", JOptionPane.WARNING_MESSAGE);
                    } else {
                         JOptionPane.showMessageDialog(this, addResult, "Add Book Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Year or Copies format. Please enter numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(this, "Database error adding book: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
            } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
            } finally {
                 setCursor(Cursor.getDefaultCursor());
                 setActionsEnabled(true);
            }
        }
    }

     // Helper to handle exceptions from SwingWorker
     private void handleWorkerException(Exception ex, String actionDescription) {
         Throwable cause = (ex instanceof ExecutionException) ? ex.getCause() : ex;
         statusLabel.setText("Error " + actionDescription);
         if (cause instanceof SQLException) {
             JOptionPane.showMessageDialog(this, "Database error while " + actionDescription + ":\n" + cause.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
         } else {
             JOptionPane.showMessageDialog(this, "An unexpected error occurred while " + actionDescription + ":\n" + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
         cause.printStackTrace(); // Log detailed error
     }

} // End of AdminDashboardPanel class
