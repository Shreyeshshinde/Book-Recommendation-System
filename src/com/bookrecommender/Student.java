package com.bookrecommender; // Assuming Student/Admin are in the main package

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


/**
 * Represents a Student user and handles student-specific operations
 * interacting with the database. Designed for use with a GUI.
 */
public class Student {
    private int userId;
    private String username;
    private String name;
    // No longer holds Connection, it will be passed into methods

    // Private constructor - instances created via static login method
    private Student(int userId, String username, String name) {
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
     * Attempts to log in a student user.
     *
     * @param conn     The active database connection.
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return A Student object if login is successful, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public static Student login(Connection conn, String username, String password) throws SQLException {
        // TODO: Implement password hashing and comparison
        String query = "SELECT UserID, Name FROM users WHERE Username = ? AND Password = ? AND Role = 'student'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Compare plain text password - VERY INSECURE
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Login successful
                return new Student(rs.getInt("UserID"), username, rs.getString("Name"));
            } else {
                // Login failed (invalid credentials or not a student)
                return null;
            }
        }
    }

    /**
     * Registers a new student user. Performs validation before inserting.
     *
     * @param conn     The active database connection.
     * @param username Desired username.
     * @param password Desired password.
     * @param name     Student's full name.
     * @param email    Student's email address.
     * @return A success message (including User ID) or an error message indicating the reason for failure.
     * @throws SQLException If an unexpected database error occurs during checks or insertion.
     */
    public static String registerStudent(Connection conn, String username, String password, String name, String email) throws SQLException {
        // 1. Basic Input Validation (Check for empty strings)
        if (username == null || username.trim().isEmpty() ||
            password == null || password.isEmpty() || // Password can't be just whitespace
            name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return "Registration Error: All fields are required.";
        }

        // Trim inputs
        username = username.trim();
        name = name.trim();
        email = email.trim();

        // 2. Email Format Validation
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        if (!emailPattern.matcher(email).matches()) {
            return "Registration Error: Invalid email format.";
        }

        // 3. Check if username already exists
        String checkUserQuery = "SELECT UserID FROM users WHERE Username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkUserQuery)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Registration Error: Username '" + username + "' already exists.";
            }
        }

        // 4. Check if email already exists
        String checkEmailQuery = "SELECT UserID FROM users WHERE Email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkEmailQuery)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Registration Error: Email address '" + email + "' is already registered.";
            }
        }

        // 5. Insert new user
        // TODO: HASH THE PASSWORD before storing it!
        String insertQuery = "INSERT INTO users (Username, Password, Role, Name, Email) VALUES (?, ?, 'student', ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Store plain password - VERY INSECURE
            pstmt.setString(3, name);
            pstmt.setString(4, email);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newUserId = rs.getInt(1);
                        return "Registration successful! Welcome, " + name + "! Your User ID is: " + newUserId;
                    } else {
                         // Should not happen if affectedRows > 0, but good to handle
                         throw new SQLException("Failed to retrieve generated User ID after insertion.");
                    }
                }
            } else {
                // Insertion failed for some reason
                return "Registration Error: Failed to create user account. Please try again.";
            }
        }
        // Catch SQLException from checks or insertion and let it propagate up
    }


    /**
     * Retrieves the list of books currently issued to this student.
     *
     * @param conn The database connection.
     * @return A List of String arrays, where each array contains [BookID, Title, Author, IssueDate, DueDate, Fine, Status]. Returns empty list if no books are issued.
     * @throws SQLException If a database error occurs.
     */
    public List<String[]> getIssuedBooks(Connection conn) throws SQLException {
        List<String[]> issuedBooksData = new ArrayList<>();
        String query = "SELECT b.BookID, b.Title, b.Author, bi.IssueDate, bi.DueDate, bi.Fine, bi.Status " +
                       "FROM book_issues bi JOIN books b ON bi.BookID = b.BookID " +
                       "WHERE bi.UserID = ? AND bi.Status != 'returned' " +
                       "ORDER BY bi.DueDate ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String[] bookData = new String[7];
                bookData[0] = rs.getString("BookID");
                bookData[1] = rs.getString("Title");
                bookData[2] = rs.getString("Author");
                bookData[3] = rs.getString("IssueDate");
                bookData[4] = rs.getString("DueDate");
                bookData[5] = String.format("%.2f", rs.getDouble("Fine")); // Format fine
                bookData[6] = rs.getString("Status");
                issuedBooksData.add(bookData);
            }
        }
        return issuedBooksData;
    }

     /**
      * Gets book recommendations for the student.
      * Calls the static method in BookRecommendationSystem.
      *
      * @param conn The database connection (needed by recommendBooks indirectly).
      * @return A List of String arrays, where each array contains [BookID, Title, Author, Genre]. Returns empty list if no recommendations.
      * @throws SQLException If a database error occurs.
      */
    public List<String[]> getRecommendations(Connection conn) throws SQLException {
        List<String[]> recommendationsData = new ArrayList<>();
        // Get recommended IDs using the logic (which now uses static maps/connection)
        List<Integer> recommendedIds = BookRecommendationSystem.recommendBooks(this.userId);

        for (int bookId : recommendedIds) {
            // Retrieve details from the static maps for efficiency
            String title = BookRecommendationSystem.getBookTitle(bookId);
            String author = BookRecommendationSystem.getBookAuthor(bookId);
            String genre = BookRecommendationSystem.getBookGenre(bookId);

            // Add data for display (e.g., in a JTable)
            recommendationsData.add(new String[]{
                String.valueOf(bookId),
                title,
                author,
                genre
            });
        }
        return recommendationsData;
    }

}

