package com.danieljoaco.storeapp.user;

import com.danieljoaco.storeapp.db.DatabaseManager;
import com.danieljoaco.storeapp.menu.forms.UserFormController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.danieljoaco.storeapp.db.ProductsDao.calculateSimilarityScore;

/**
 * Data Access Object for user-related operations in the database.
 */
public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    // SQL Constants
    private static final String SQL_CHECK_USER_EXISTS = "SELECT COUNT(*) FROM users WHERE id_user = ? OR email = ?";
    private static final String SQL_INSERT_USER = "INSERT INTO users (id_user, name, email, password, type_id, created_at, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_CHECK_ADMIN_EXISTS = "SELECT COUNT(*) FROM users WHERE type_id = 1";
    private static final String SQL_FIND_USER_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String SQL_SEARCH_USERS = """
            SELECT DISTINCT
                u.id_user,
                u.name,
                u.email,
                u.type_id,
                u.created_at,
                u.phone_number
            FROM users u
            WHERE u.email LIKE ? OR u.name LIKE ? OR u.id_user LIKE ?
            """;
    private static final String SQL_GET_ALL_USERS = "SELECT * FROM users";
    private static final String SQL_UPDATE_USER = "UPDATE users SET  id = ?, name = ?, email = ?, type_id = ?, phone_number = ? WHERE id_user = ?";
    private static final String SQL_DELETE_USER = "DELETE FROM users WHERE id_user = ?";

    // Minimum similarity threshold for search results
    private static final int SIMILARITY_THRESHOLD = 80;

    /**
     * Converts user type to a numeric ID for the database.
     *
     * @param user User to convert
     * @return Numeric ID of user type
     * @throws IllegalArgumentException If user type is invalid
     */
    private static int getUserTypeId(User user) {
        if (user instanceof Admin) return 1;
        if (user instanceof SupportAgent) return 2;
        if (user instanceof Customer) return 3;

        logger.error("❌ Invalid user type");
        throw new IllegalArgumentException("Invalid user type");
    }

    /**
     * Creates a user object from a ResultSet
     *
     * @param rs ResultSet with user data
     * @return Created User object
     * @throws SQLException If there's an error reading data
     * @throws IllegalStateException If the user type is unknown
     */
    private static User createUserFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id_user");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String pwdHash = rs.getString("password");
        int typeUser = rs.getInt("type_id");
        LocalDate createdAt = rs.getDate("created_at").toLocalDate();
        String phoneNumber = rs.getString("phone_number");

        return switch (typeUser) {
            case 1 -> Admin.createAdminFromDb(id, email, pwdHash, name, createdAt);
            case 2 -> SupportAgent.createAgentFromDb(id, email, pwdHash, name, createdAt);
            case 3 -> new Customer(id, email, pwdHash, name, phoneNumber, createdAt);
            default -> throw new IllegalStateException("Unknown user type: " + typeUser);
        };
    }

    /**
     * Saves a user in the database.
     * Validates if the user already exists by checking the ID and email.
     *
     * @param user User to save
     * @return true if the user was successfully saved, false otherwise
     */
    public static boolean saveUser(User user) {
        if (user == null) {
            logger.error("❌ Null user");
            return false;
        }

        int userType = getUserTypeId(user);
        String phoneNumber = (user instanceof Customer) ? ((Customer) user).getPhoneNumber() : null;

        try (Connection conn = DatabaseManager.connect()) {
            // Check if user already exists
            if (userExists(conn, user.getId(), user.getEmail())) {
                logger.warn("⚠️ Error: ID or email already registered");
                return false;
            }

            // Insert new user
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_USER)) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getPasswordHash());
                pstmt.setInt(5, userType);
                pstmt.setDate(6, Date.valueOf(user.getCreatedAt()));
                pstmt.setString(7, phoneNumber);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    logger.info("✅ User saved: {}", user.getEmail());
                    return true;
                } else {
                    logger.warn("⚠️ User was not inserted");
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error saving user: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a user with the provided ID or email already exists
     *
     * @param conn Database connection
     * @param id User ID
     * @param email User email
     * @return true if the user exists, false otherwise
     * @throws SQLException If there's an error in the query
     */
    private static boolean userExists(Connection conn, String id, String email) throws SQLException {
        try (PreparedStatement checkStmt = conn.prepareStatement(SQL_CHECK_USER_EXISTS)) {
            checkStmt.setString(1, id);
            checkStmt.setString(2, email);
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if an admin user exists in the database.
     *
     * @return true if an admin exists, false otherwise
     */
    public static boolean adminExists() {
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_CHECK_ADMIN_EXISTS);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("❌ Error verifying admin: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Finds a user by their email address.
     *
     * @param email Email of the user to find
     * @return Optional with the user if found, Optional.empty() otherwise
     */
    public static User findUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_USER_BY_EMAIL)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error finding user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Searches for users that match the search pattern in ID, name, or email.
     *
     * @param query Search pattern
     * @return Observable list of users matching the pattern
     */
    public static ObservableList<BasicUserInfoDb> findUser(String query) {
        if (query == null || query.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        String searchPattern = "%" + query.replace("_", "\\_").replace("%", "\\%") + "%";
        Map<BasicUserInfoDb, Integer> userScores = new HashMap<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SEARCH_USERS)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BasicUserInfoDb userInfo = new BasicUserInfoDb(
                            rs.getString("id_user"),
                            rs.getString("email"),
                            rs.getString("name"),
                            rs.getInt("type_id"),
                            rs.getDate("created_at").toLocalDate(),
                            rs.getString("phoneNumber")
                    );

                    // Calculate similarity score
                    int idScore = calculateSimilarityScore(query, userInfo.id());
                    int nameScore = calculateSimilarityScore(query, userInfo.name());
                    int emailScore = calculateSimilarityScore(query, userInfo.email());
                    int maxScore = Math.max(Math.max(idScore, nameScore), emailScore);

                    userScores.put(userInfo, maxScore);
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error searching users: {}", e.getMessage());
        }

        // Filter and sort results by similarity score
        return userScores.entrySet().stream()
                .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                .sorted(Map.Entry.<BasicUserInfoDb, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Gets all users from the database.
     *
     * @return List of all users
     */
    public static List<User> getAllUsers() {
        List<User> usersList = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_ALL_USERS);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                usersList.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("❌ Error getting all users: {}", e.getMessage());
        }

        return usersList;
    }

    /**
     * Updates user information in the database.
     *
     * @param basicUserInfo Basic user information to update
     * @return true if the update was successful, false otherwise
     */
    public static void updateUserToDb(UserFormController.BasicUserInfo basicUserInfo) {
        if (basicUserInfo == null) {
            logger.error("❌ Null user information");
        }

        try (Connection conn = DatabaseManager.connect()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_USER)) {
                    assert basicUserInfo != null;
                    pstmt.setString(1, basicUserInfo.id());
                    pstmt.setString(2, basicUserInfo.name());
                    pstmt.setString(3, basicUserInfo.email());
                    pstmt.setInt(4, basicUserInfo.typeUserId());
                    pstmt.setString(5, basicUserInfo.phoneNumber());
                    pstmt.setString(6, basicUserInfo.id());


                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        conn.commit();
                        logger.info("✅ User with ID {} updated successfully", basicUserInfo.id());
                    } else {
                        conn.rollback();
                        logger.warn("⚠️ No user found with ID {}", basicUserInfo.id());
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ Error updating user: {}", e.getMessage());
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("❌ Connection error: {}", e.getMessage());
        }
    }

    /**
     * Deletes a user from the database by their ID.
     *
     * @param id ID of the user to delete
     */
    public static void deleteUserToDb(String id) {
        if (id == null || id.isEmpty()) {
            logger.error("❌ Invalid user ID");
        }

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_USER)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✅ User with ID {} deleted successfully", id);
            } else {
                logger.warn("⚠️ No user found with ID {}", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Error deleting user: {}", e.getMessage());
        }
    }

    /**
     * Basic information about a user in the database.
     */
    public record BasicUserInfoDb(String id, String email, String name, int typeUserId, LocalDate createAt, String phoneNumber) {}
}