package com.bookrecommender.ui;

import com.bookrecommender.BookRecommendationSystem;
import com.bookrecommender.Student;

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
import java.util.concurrent.ExecutionException;

/**
 * JPanel for the Student Dashboard with a more modern look.
 */
public class StudentDashboardPanel extends JPanel implements ActionListener {

    private BookRecGUI mainGUI;
    private Student currentStudent; // Store the logged-in student

    // UI Components
    private JLabel welcomeLabel;
    private JButton recommendationsButton;
    private JButton issuedBooksButton;
    private JButton logoutButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JLabel statusLabel; // Label to show loading status

    // Define Colors (Consistent Theme)
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color HEADER_BACKGROUND_COLOR = new Color(233, 236, 239); // Light grey for header/footer
    private static final Color TABLE_HEADER_COLOR = new Color(52, 58, 64); // Dark grey for table header
    private static final Color BORDER_COLOR = new Color(222, 226, 230); // **** ADD THIS LINE **** Light grey border
    private static final Color BUTTON_PRIMARY_COLOR = new Color(0, 123, 255);
    private static final Color BUTTON_SECONDARY_COLOR = new Color(40, 167, 69); // Green
    private static final Color BUTTON_DANGER_COLOR = new Color(220, 20, 60); // Crimson
    private static final Color TEXT_COLOR = new Color(33, 37, 41);

    // Define Fonts (Consistent Theme)
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font WELCOME_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font TABLE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font TABLE_HEADER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font STATUS_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 12);


    // Table Column Headers
    private final String[] recommendationsColumns = {"Book ID", "Title", "Author", "Genre"};
    private final String[] issuedBooksColumns = {"Book ID", "Title", "Author", "Issued Date", "Due Date", "Fine ($)", "Status"};

    public StudentDashboardPanel(BookRecGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add padding
        setBackground(BACKGROUND_COLOR);

        // --- Top Panel (Header) ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 0)); // Add horizontal gap
        topPanel.setOpaque(true); // Make opaque to set background
        topPanel.setBackground(HEADER_BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding inside header

        welcomeLabel = new JLabel("Welcome, Student!", JLabel.LEFT);
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setForeground(TEXT_COLOR);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        logoutButton = createStyledButton("Logout", BUTTON_DANGER_COLOR);
        logoutButton.addActionListener(this);
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- Center Panel (Results Table) ---
        tableModel = new DefaultTableModel() {
             // Make cells non-editable by default
             @Override
             public boolean isCellEditable(int row, int column) {
                return false;
             }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(TABLE_FONT);
        resultsTable.setRowHeight(24); // Increased row height
        resultsTable.setGridColor(BORDER_COLOR); // Lighter grid lines
        resultsTable.setShowGrid(true); // Ensure grid lines are visible
        resultsTable.setIntercellSpacing(new Dimension(0, 1)); // Subtle vertical spacing
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow selecting only one row
        resultsTable.setBackground(Color.WHITE); // White table background
        resultsTable.setForeground(TEXT_COLOR); // Dark text
        resultsTable.setSelectionBackground(BUTTON_PRIMARY_COLOR.brighter()); // Selection color
        resultsTable.setSelectionForeground(Color.WHITE);

        // Style Table Header
        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false); // Prevent column reordering

        scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR)); // Border around scrollpane (NOW WORKS)
        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel (Footer with Actions and Status) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(HEADER_BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        recommendationsButton = createStyledButton("Get Recommendations", BUTTON_PRIMARY_COLOR);
        recommendationsButton.addActionListener(this);
        buttonPanel.add(recommendationsButton);

        issuedBooksButton = createStyledButton("View My Issued Books", BUTTON_SECONDARY_COLOR);
        issuedBooksButton.addActionListener(this);
        buttonPanel.add(issuedBooksButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready", JLabel.LEFT); // Status label
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(Color.GRAY);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);


        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Helper to create styled buttons
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(text.length() > 18 ? 200 : 180, 35)); // Adjust width based on text
        return button;
    }


    /**
     * Sets the currently logged-in student and updates the welcome message.
     * Called after successful student login.
     * @param student The logged-in Student object.
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        if (student != null) {
            welcomeLabel.setText("Welcome, " + student.getName() + " (ID: " + student.getUserId() + ")");
            clearTable(); // Clear previous results
            statusLabel.setText("Ready");
        } else {
            welcomeLabel.setText("Welcome, Student!"); // Reset
            clearTable();
            statusLabel.setText("Logged out");
        }
    }

    /**
     * Clears the results table and sets columns to empty.
     */
    private void clearTable() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        statusLabel.setText("Table cleared");
    }

    /**
     * Updates the JTable with new data and column headers.
     * @param data         The data rows (List of String arrays).
     * @param columnNames The names for the table columns.
     */
    private void updateTable(List<String[]> data, String[] columnNames) {
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0); // Clear existing rows
        if (data != null) {
            for (String[] row : data) {
                tableModel.addRow(row);
            }
        }
        // Adjust column widths (optional, can be complex)
        // Consider using a utility like TableColumnAdjuster if needed
        statusLabel.setText("Displayed " + (data != null ? data.size() : 0) + " items.");
    }

    // Disable buttons during background task
    private void setButtonsEnabled(boolean enabled) {
        recommendationsButton.setEnabled(enabled);
        issuedBooksButton.setEnabled(enabled);
        logoutButton.setEnabled(enabled);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentStudent == null && e.getSource() != logoutButton) {
            JOptionPane.showMessageDialog(this, "Error: No student is logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object source = e.getSource();
        setButtonsEnabled(false); // Disable buttons
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Show wait cursor

        if (source == recommendationsButton) {
            statusLabel.setText("Loading recommendations...");
            // Use SwingWorker for DB operation
            SwingWorker<List<String[]>, Void> worker = new SwingWorker<List<String[]>, Void>() {
                @Override
                protected List<String[]> doInBackground() throws Exception {
                    Connection conn = BookRecommendationSystem.getConnection();
                    return currentStudent.getRecommendations(conn);
                }

                @Override
                protected void done() {
                    try {
                        List<String[]> recommendations = get();
                        updateTable(recommendations, recommendationsColumns);
                        if (recommendations.isEmpty()) {
                            JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                                "No recommendations found based on your history.\nIssue some books first!",
                                "No Recommendations", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        handleWorkerException(ex, "loading recommendations");
                    } finally {
                        setButtonsEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };
            worker.execute();

        } else if (source == issuedBooksButton) {
             statusLabel.setText("Loading issued books...");
             // Use SwingWorker for DB operation
             SwingWorker<List<String[]>, Void> worker = new SwingWorker<List<String[]>, Void>() {
                @Override
                protected List<String[]> doInBackground() throws Exception {
                    Connection conn = BookRecommendationSystem.getConnection();
                    return currentStudent.getIssuedBooks(conn);
                }
                 @Override
                protected void done() {
                    try {
                        List<String[]> issuedBooks = get();
                        updateTable(issuedBooks, issuedBooksColumns);
                        if (issuedBooks.isEmpty()) {
                            JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                                "You have no books currently issued.",
                                "No Issued Books", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        handleWorkerException(ex, "loading issued books");
                    } finally {
                        setButtonsEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
             };
             worker.execute();

        } else if (source == logoutButton) {
            int confirmed = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirmed == JOptionPane.YES_OPTION) {
                setStudent(null); // Clear current student
                mainGUI.showPanel(BookRecGUI.LOGIN_PANEL); // Go back to login
            }
            // Re-enable buttons if logout cancelled
            setButtonsEnabled(true);
            setCursor(Cursor.getDefaultCursor());
        } else {
             // If action wasn't handled, re-enable buttons/cursor
             setButtonsEnabled(true);
             setCursor(Cursor.getDefaultCursor());
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
}
