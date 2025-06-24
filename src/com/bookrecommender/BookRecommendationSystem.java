package com.bookrecommender;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
// No longer needs regex/Pattern here, moved to UI/Student class
// No longer needs console-specific imports like Scanner or time imports here

/**
 * Manages database connection, loading book data, and providing recommendation logic
 * for the Book Recommendation System GUI.
 * The original graph logic is kept but unused by the current recommendation method.
 */
public class BookRecommendationSystem {
    // MySQL connection details (Consider externalizing these)
    private static final String URL = "jdbc:mysql://localhost:3306/cp"; // Replace 'cp' with your actual database name
    private static final String USER = "root";
    private static final String PASSWORD = "password"; // Replace with your actual password

    // Static connection object accessible by UI components
    private static Connection conn;

    // Static maps to hold book details loaded from the database (accessible via getters)
    private static Map<Integer, String> bookTitles = new HashMap<>();
    private static Map<Integer, String> bookAuthors = new HashMap<>();
    private static Map<Integer, String> bookGenres = new HashMap<>();
    // Graph and years map kept for potential future use or compatibility, but not used in current recommendations
    private static Map<Integer, List<Integer>> graph = new HashMap<>();
    private static Map<Integer, Integer> bookYears = new HashMap<>();

    /**
     * Establishes the database connection.
     * Should be called once when the GUI application starts.
     * @throws SQLException if the connection fails.
     */
    public static void connectDatabase() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established successfully.");
            } catch (SQLException e) {
                System.err.println("FATAL: Database connection failed: " + e.getMessage());
                throw e; // Re-throw to be handled by the GUI launcher
            }
        }
    }

    /**
     * Closes the database connection.
     * Should be called when the GUI application exits.
     */
    public static void closeDatabase() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Gets the active database connection.
     * Ensures connectDatabase() was called first.
     * @return The active Connection object.
     * @throws SQLException if the connection is null or closed.
     */
    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            // Optionally try to reconnect or throw a more specific error
             System.err.println("Database connection is not available. Trying to reconnect...");
             connectDatabase(); // Attempt to reconnect
             if (conn == null || conn.isClosed()) {
                 throw new SQLException("Database connection is not available.");
             }
        }
        return conn;
    }


    /**
     * Loads book data from the database into static maps.
     * Builds the graph (currently unused by recommendation logic).
     * Should be called after connectDatabase().
     * @throws SQLException if a database access error occurs.
     */
    public static void loadBooks() throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Cannot load books, database is not connected.");
        }
        String query = "SELECT BookID, Title, Author, Genre, Publication FROM books";

        // Clear existing maps before loading
        bookTitles.clear();
        bookAuthors.clear();
        bookGenres.clear();
        bookYears.clear();
        graph.clear(); // Clear graph too

        int bookCount = 0;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                bookCount++;
                int bookId = rs.getInt("BookID");
                // Store details in respective maps (original case for display)
                bookTitles.put(bookId, rs.getString("Title"));
                bookAuthors.put(bookId, rs.getString("Author"));
                bookGenres.put(bookId, rs.getString("Genre"));
                bookYears.put(bookId, rs.getInt("Publication"));
                graph.putIfAbsent(bookId, new ArrayList<>()); // Initialize graph entry
            }
        }
         System.out.println("Loaded " + bookCount + " books from the database.");

        // Build graph (kept for compatibility, unused by current recommendBooks)
        buildGraph(); // Encapsulated graph building
    }

    /**
     * Helper method to build the graph based on loaded book data.
     * Connects books if they share Author, Genre, or are published within 5 years.
     * (Unused by the current recommendation logic).
     */
    private static void buildGraph() {
         System.out.println("Building book graph (unused by current recommendation)...");
         int edges = 0;
         for (int book1 : bookTitles.keySet()) {
             for (int book2 : bookTitles.keySet()) {
                 if (book1 >= book2) continue; // Avoid duplicates and self-loops

                 String author1 = bookAuthors.get(book1);
                 String author2 = bookAuthors.get(book2);
                 String genre1 = bookGenres.get(book1);
                 String genre2 = bookGenres.get(book2);
                 Integer year1 = bookYears.get(book1);
                 Integer year2 = bookYears.get(book2);

                 boolean sameAuthor = author1 != null && author1.equals(author2);
                 boolean sameGenre = genre1 != null && genre1.equals(genre2);
                 boolean closeYear = year1 != null && year2 != null && Math.abs(year1 - year2) <= 5;

                 if (sameAuthor || sameGenre || closeYear) {
                     graph.computeIfAbsent(book1, k -> new ArrayList<>()).add(book2);
                     graph.computeIfAbsent(book2, k -> new ArrayList<>()).add(book1);
                     edges++;
                 }
             }
         }
         System.out.println("Graph building complete. Edges added: " + edges);
    }


    /**
     * Recommends books based SOLELY on matching authors and genres (case-insensitive)
     * of the user's issued book history.
     *
     * @param userId The ID of the user for whom to generate recommendations.
     * @return A list of up to 5 recommended BookIDs. Returns empty list if no history or no recommendations found.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Integer> recommendBooks(int userId) throws SQLException {
        // 1. Get user's book history (issued books)
        Set<Integer> issuedBookIds = new HashSet<>();
        String historyQuery = "SELECT DISTINCT BookID FROM user_book_history WHERE UserID = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(historyQuery)) { // Use getConnection()
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                issuedBookIds.add(rs.getInt("BookID"));
            }
        }

        if (issuedBookIds.isEmpty()) {
            return new ArrayList<>(); // No history, no recommendations
        }

        // 2. Get genres and authors of the issued books (LOWERCASE for comparison)
        Set<String> issuedGenresLower = new HashSet<>();
        Set<String> issuedAuthorsLower = new HashSet<>();
        for (int issuedBookId : issuedBookIds) {
            String genre = bookGenres.get(issuedBookId);
            String author = bookAuthors.get(issuedBookId);
            if (genre != null && !genre.trim().isEmpty()) {
                issuedGenresLower.add(genre.trim().toLowerCase());
            }
            if (author != null && !author.trim().isEmpty()) {
                issuedAuthorsLower.add(author.trim().toLowerCase());
            }
        }

        // 3. Score potential recommendations (case-insensitive)
        Map<Integer, Integer> recommendationScores = new HashMap<>();
        for (int candidateBookId : bookTitles.keySet()) {
            if (!issuedBookIds.contains(candidateBookId)) { // Exclude already issued books
                int currentScore = 0;
                String candidateGenre = bookGenres.get(candidateBookId);
                String candidateAuthor = bookAuthors.get(candidateBookId);
                String candidateGenreLower = (candidateGenre != null) ? candidateGenre.trim().toLowerCase() : null;
                String candidateAuthorLower = (candidateAuthor != null) ? candidateAuthor.trim().toLowerCase() : null;

                // Score based on genre match
                if (candidateGenreLower != null && !candidateGenreLower.isEmpty() && issuedGenresLower.contains(candidateGenreLower)) {
                    currentScore++;
                }
                // Score based on author match
                if (candidateAuthorLower != null && !candidateAuthorLower.isEmpty() && issuedAuthorsLower.contains(candidateAuthorLower)) {
                    currentScore++;
                }

                if (currentScore > 0) {
                    recommendationScores.put(candidateBookId, currentScore);
                }
            }
        }

        // 4. Sort books by score (descending) and return the top 5 IDs
        return recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // --- Static Getters for Book Data (Used by UI) ---

    public static String getBookTitle(int bookId) {
        return bookTitles.getOrDefault(bookId, "Unknown Title");
    }

    public static String getBookAuthor(int bookId) {
        return bookAuthors.getOrDefault(bookId, "Unknown Author");
    }

    public static String getBookGenre(int bookId) {
        return bookGenres.getOrDefault(bookId, "Unknown Genre");
    }

     public static Map<Integer, String> getAllBookTitles() {
        return Collections.unmodifiableMap(bookTitles); // Return unmodifiable map
    }

}
