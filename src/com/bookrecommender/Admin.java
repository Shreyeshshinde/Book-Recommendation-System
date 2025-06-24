package com.bookrecommender; // Ensure this package declaration is present

// Add all necessary imports
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// No need to import BookRecommendationSystem if Admin doesn't directly call static methods from it anymore
// Correction: addNewBook calls BookRecommendationSystem.loadBooks(), so the import IS needed.
import com.bookrecommender.BookRecommendationSystem;


/**
 * Represents an Admin user and handles admin-specific operations
 * interacting with the database. Designed for use with a GUI.
 */
public class Admin { // Make the class public
    private int userId;
    private String username;
    private String name;
    // No longer holds Connection

    // Private constructor - instances created via static login method
    private Admin(int userId, String username, String name) {
        this.userId = userId;
        this.username = username;
        this.name = name;
    }

    // --- Getters ---
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    // --- Static Methods for GUI Interaction ---

    /**
     * Attempts to log in an admin user.
     *
     * @param conn     The active database connection.
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return An Admin object if login is successful, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public static Admin login(Connection conn, String username, String password) throws SQLException {
        // TODO: Implement password hashing and comparison
        String query = "SELECT UserID, Name FROM users WHERE Username = ? AND Password = ? AND Role = 'admin'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Compare plain text password - VERY INSECURE
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Login successful
                return new Admin(rs.getInt("UserID"), username, rs.getString("Name"));
            } else {
                // Login failed (invalid credentials or not an admin)
                return null;
            }
        }
    }

    /**
     * Retrieves the User ID for a given username and role.
     * @param conn The database connection.
     * @param username The username to lookup.
     * @param role The role to match (e.g., 'student').
     * @return The UserID if found, otherwise -1.
     * @throws SQLException If a database error occurs.
     */
    private static int getUserIdByUsername(Connection conn, String username, String role) throws SQLException {
        String query = "SELECT UserID FROM users WHERE Username = ? AND Role = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("UserID") : -1; // Return UserID if found, else -1
        }
    }


    /**
     * Issues a book to a specified student. Performs checks before issuing.
     *
     * @param conn            The database connection.
     * @param studentUsername The username of the student receiving the book.
     * @param bookId          The ID of the book to issue.
     * @return A success or error message string.
     * @throws SQLException If an unexpected database error occurs during checks or transaction.
     */
    public static String issueBookToStudent(Connection conn, String studentUsername, int bookId) throws SQLException {
        // 1. Validate student username
        if (studentUsername == null || studentUsername.trim().isEmpty()) {
            return "Error: Student username cannot be empty.";
        }
        studentUsername = studentUsername.trim();

        // 2. Get student ID
        int studentId = getUserIdByUsername(conn, studentUsername, "student");
        if (studentId == -1) {
            return "Error: Student username '" + studentUsername + "' not found.";
        }

        // 3. Check Book Existence and Availability
        String bookTitle = null;
        String bookCheckQuery = "SELECT Title, AvailableCopies FROM books WHERE BookID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(bookCheckQuery)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                return "Error: Book ID " + bookId + " not found.";
            }
            bookTitle = rs.getString("Title");
            int availableCopies = rs.getInt("AvailableCopies");
            if (availableCopies < 1) {
                return "Error: No copies of '" + bookTitle + "' (ID: " + bookId + ") are currently available.";
            }
        }

        // 4. Check if student already has this book issued (and not returned)
        String checkExistingIssueQuery = "SELECT IssueID FROM book_issues WHERE UserID = ? AND BookID = ? AND Status != 'returned'";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkExistingIssueQuery)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, bookId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next()) {
                return "Error: Student '" + studentUsername + "' already has book '" + bookTitle + "' (ID: " + bookId + ") issued.";
            }
        }

        // 5. Perform Issue Transaction
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(14); // 14-day loan period

        conn.setAutoCommit(false); // Start transaction
        try {
            // a. Insert issue record
            String issueQuery = "INSERT INTO book_issues (BookID, UserID, IssueDate, DueDate, Status) VALUES (?, ?, ?, ?, 'issued')";
            try (PreparedStatement issueStmt = conn.prepareStatement(issueQuery)) {
                issueStmt.setInt(1, bookId);
                issueStmt.setInt(2, studentId);
                issueStmt.setDate(3, Date.valueOf(issueDate));
                issueStmt.setDate(4, Date.valueOf(dueDate));
                issueStmt.executeUpdate();
            }

            // b. Update available copies count
            String updateQuery = "UPDATE books SET AvailableCopies = AvailableCopies - 1 WHERE BookID = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
            }

            // c. Add to user history
            String historyQuery = "INSERT INTO user_book_history (UserID, BookID, InteractionType, Timestamp) VALUES (?, ?, 'issued', NOW())";
            try (PreparedStatement historyStmt = conn.prepareStatement(historyQuery)) {
                historyStmt.setInt(1, studentId);
                historyStmt.setInt(2, bookId);
                historyStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            // Reload books in main system to reflect count change? Optional.
            // BookRecommendationSystem.loadBooks();
            return "Success: Book '" + bookTitle + "' (ID: " + bookId + ") issued to '" + studentUsername + "'. Due: " + dueDate;

        } catch (SQLException e) {
            conn.rollback(); // Rollback transaction on error
            System.err.println("Transaction failed during book issue: " + e.getMessage());
            // Return a specific error or re-throw
            return "Error: Database transaction failed during issue. Reason: " + e.getMessage();
        } finally {
            conn.setAutoCommit(true); // Restore default commit behavior
        }
    }


    /**
     * Calculates and updates fines for a specific student's overdue books.
     * Changes status of newly overdue books to 'overdue'.
     *
     * @param conn            The database connection.
     * @param studentUsername The username of the student.
     * @return A status message detailing calculated fines and total outstanding fine.
     * @throws SQLException If an unexpected database error occurs.
     */
    public static String calculateFineForStudent(Connection conn, String studentUsername) throws SQLException {
        // 1. Validate username
        if (studentUsername == null || studentUsername.trim().isEmpty()) {
            return "Error: Student username cannot be empty.";
        }
        studentUsername = studentUsername.trim();

        // 2. Get student ID
        int studentId = getUserIdByUsername(conn, studentUsername, "student");
        if (studentId == -1) {
            return "Error: Student username '" + studentUsername + "' not found.";
        }

        // 3. Find newly overdue books (Status = 'issued' and DueDate < Today)
        String query = "SELECT bi.IssueID, b.Title, bi.DueDate " +
                       "FROM book_issues bi JOIN books b ON bi.BookID = b.BookID " +
                       "WHERE bi.UserID = ? AND bi.Status = 'issued' AND bi.DueDate < CURDATE()";

        Map<Integer, Double> finesToUpdate = new HashMap<>();
        double totalNewFineCalculated = 0;
        StringBuilder fineDetails = new StringBuilder();
        final double FINE_RATE_PER_DAY = 0.50; // Example fine rate

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int issueId = rs.getInt("IssueID");
                String title = rs.getString("Title");
                LocalDate dueDate = rs.getDate("DueDate").toLocalDate();
                long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());

                if (daysOverdue > 0) {
                    double fine = daysOverdue * FINE_RATE_PER_DAY;
                    finesToUpdate.put(issueId, fine);
                    totalNewFineCalculated += fine;
                    fineDetails.append(String.format("- '%s' (Issue %d): %d days overdue, New Fine: $%.2f\n",
                                                    title, issueId, daysOverdue, fine));
                }
            }
        }

        // 4. Update Database if new fines were calculated
        if (!finesToUpdate.isEmpty()) {
            String updateQuery = "UPDATE book_issues SET Fine = ?, Status = 'overdue' WHERE IssueID = ?";
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                for (Map.Entry<Integer, Double> entry : finesToUpdate.entrySet()) {
                    updateStmt.setDouble(1, entry.getValue());
                    updateStmt.setInt(2, entry.getKey());
                    updateStmt.addBatch();
                }
                updateStmt.executeBatch();
                conn.commit();
                fineDetails.insert(0, "Calculated and updated fines for:\n");
                fineDetails.append(String.format("Total New Fine Added: $%.2f\n", totalNewFineCalculated));
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error updating fines: " + e.getMessage());
                return "Error: Database transaction failed while updating fines. Reason: " + e.getMessage();
            } finally {
                conn.setAutoCommit(true);
            }
        } else {
            fineDetails.append("No newly overdue books found for '").append(studentUsername).append("'.\n");
        }

        // 5. Get Total Outstanding Fine
        String fineCheckQuery = "SELECT SUM(Fine) AS TotalFine FROM book_issues WHERE UserID = ? AND Status != 'returned'";
        try (PreparedStatement fineStmt = conn.prepareStatement(fineCheckQuery)) {
            fineStmt.setInt(1, studentId);
            ResultSet fineRs = fineStmt.executeQuery();
            if (fineRs.next()) {
                double totalOutstandingFine = fineRs.getDouble("TotalFine");
                fineDetails.append(String.format("Total Current Outstanding Fine: $%.2f", totalOutstandingFine));
            }
        }

        return fineDetails.toString();
    }


    /**
     * Retrieves a list of all currently issued (not returned) books across all students.
     *
     * @param conn The database connection.
     * @return A List of String arrays for JTable display: [IssueID, Student Username, Book Title, BookID, Issue Date, Due Date, Status, Fine]. Empty list if none.
     * @throws SQLException If a database error occurs.
     */
    public static List<String[]> getAllIssuedBooks(Connection conn) throws SQLException {
        List<String[]> allIssuedBooksData = new ArrayList<>();
        String query = "SELECT bi.IssueID, u.Username AS StudentUsername, b.Title, b.BookID, bi.IssueDate, bi.DueDate, bi.Status, bi.Fine " +
                       "FROM book_issues bi " +
                       "JOIN books b ON bi.BookID = b.BookID " +
                       "JOIN users u ON bi.UserID = u.UserID " +
                       "WHERE bi.Status != 'returned' " +
                       "ORDER BY u.Username, bi.DueDate";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                allIssuedBooksData.add(new String[]{
                    rs.getString("IssueID"),
                    rs.getString("StudentUsername"),
                    rs.getString("Title"),
                    rs.getString("BookID"),
                    rs.getString("IssueDate"),
                    rs.getString("DueDate"),
                    rs.getString("Status"),
                    String.format("%.2f", rs.getDouble("Fine"))
                });
            }
        }
        return allIssuedBooksData;
    }

    /**
     * Adds a new book to the database.
     *
     * @param conn          The database connection.
     * @param title         Book title.
     * @param author        Book author.
     * @param genre         Book genre.
     * @param year          Publication year.
     * @param totalCopies   Total number of copies.
     * @return A success or error message string.
     * @throws SQLException If an unexpected database error occurs.
     */
    public static String addNewBook(Connection conn, String title, String author, String genre, int year, int totalCopies) throws SQLException {
        // Basic validation
        if (title == null || title.trim().isEmpty() ||
            author == null || author.trim().isEmpty() ||
            genre == null || genre.trim().isEmpty()) {
            return "Error: Title, Author, and Genre cannot be empty.";
        }
         if (year <= 0 || year > LocalDate.now().getYear() + 5) { // Basic year check
             return "Error: Invalid Publication Year.";
         }
         if (totalCopies <= 0) {
             return "Error: Total Copies must be a positive number.";
         }

        // Optional: Check if book already exists (based on title/author - case insensitive)
        String checkBookExistsQuery = "SELECT BookID FROM books WHERE LOWER(Title) = ? AND LOWER(Author) = ?";
         try (PreparedStatement checkStmt = conn.prepareStatement(checkBookExistsQuery)) {
             checkStmt.setString(1, title.trim().toLowerCase());
             checkStmt.setString(2, author.trim().toLowerCase());
             ResultSet rs = checkStmt.executeQuery();
             if (rs.next()) {
                 // Consider returning a specific code or message if it exists, let UI decide to warn/prevent
                 return "Warning: A book with the same title and author already exists (ID: " + rs.getInt("BookID") + "). Addition skipped.";
                 // Or proceed after warning in UI
             }
         }


        String insertQuery = "INSERT INTO books (Title, Author, Genre, Publication, TotalCopies, AvailableCopies) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, title.trim());
            pstmt.setString(2, author.trim());
            pstmt.setString(3, genre.trim());
            pstmt.setInt(4, year);
            pstmt.setInt(5, totalCopies);
            pstmt.setInt(6, totalCopies); // Initially, all copies are available
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Reload books into memory map after successful addition
                BookRecommendationSystem.loadBooks(); // Call static method
                return "Success: Book '" + title.trim() + "' added successfully.";
            } else {
                return "Error: Failed to add the book to the database.";
            }
        }
    }


    /**
     * Retrieves a list of all registered student users.
     *
     * @param conn The database connection.
     * @return A List of String arrays for JTable display: [UserID, Username, Name, Email]. Empty list if none.
     * @throws SQLException If a database error occurs.
     */
    public static List<String[]> getAllStudents(Connection conn) throws SQLException {
        List<String[]> studentsData = new ArrayList<>();
        String query = "SELECT UserID, Username, Name, Email FROM users WHERE Role = 'student' ORDER BY UserID";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                studentsData.add(new String[]{
                    rs.getString("UserID"),
                    rs.getString("Username"),
                    rs.getString("Name"),
                    rs.getString("Email")
                });
            }
        }
        return studentsData;
    }

} // End of Admin class
