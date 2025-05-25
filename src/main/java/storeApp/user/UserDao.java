package storeApp.user;

import storeApp.menu.forms.UserFormController;
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

import static storeApp.db.Utils.*;

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
    private static final String SQL_UPDATE_USER = "UPDATE users SET name = ?, email = ?, type_id = ?, phone_number = ? WHERE id_user = ?";
    private static final String SQL_DELETE_USER = "DELETE FROM users WHERE id_user = ?";

    // Minimum similarity threshold for search results
    private static final int SIMILARITY_THRESHOLD = 80;

    /**
     * Converts user type to a numeric ID for the database.
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
     * Saves a user in the database using Utils transaction management.
     */
    public static boolean saveUser(User user) {
        if (user == null) {
            logger.error("❌ Null user");
            return false;
        }

        int userType = getUserTypeId(user);
        String phoneNumber = (user instanceof Customer) ? ((Customer) user).getPhoneNumber() : null;

        return executeTransactionWithResult(conn -> {
            // Check if user already exists
            if (userExists(conn, user.getId(), user.getEmail())) {
                logger.warn("⚠️ Error: ID or email already registered");
                throw new SQLException("User already exists");
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
        }, false);
    }

    /**
     * Checks if a user with the provided ID or email already exists
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
     */
    public static boolean adminExists() {
        return executeTransactionWithResult(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_CHECK_ADMIN_EXISTS);
                 ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }, false);
    }

    /**
     * Finds a user by their email address.
     */
    public static User findUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        return executeTransactionWithResult(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_USER_BY_EMAIL)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return createUserFromResultSet(rs);
                    }
                }
            }
            return null;
        }, null);
    }

    /**
     * Finds a user by their ID.
     */
    public static User findUserById(int id) {
        if (id < 0) {
            return null;
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        return executeTransactionWithResult(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return createUserFromResultSet(rs);
                    }
                }
            }
            return null;
        }, null);
    }

    /**
     * Searches for users that match the search pattern in ID, name, or email.
     */
    public static ObservableList<BasicUserInfoDb> findUser(String query) {
        if (query == null || query.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        String searchPattern = "%" + query.replace("_", "\\_").replace("%", "\\%") + "%";

        return executeTransactionWithResult(conn -> {
            Map<BasicUserInfoDb, Integer> userScores = new HashMap<>();

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_SEARCH_USERS)) {
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
                                rs.getString("phone_number")
                        );

                        // Calculate similarity score
                        int idScore = calculateSimilarityScore(query, userInfo.id());
                        int nameScore = calculateSimilarityScore(query, userInfo.name());
                        int emailScore = calculateSimilarityScore(query, userInfo.email());
                        int maxScore = Math.max(Math.max(idScore, nameScore), emailScore);

                        userScores.put(userInfo, maxScore);
                    }
                }
            }

            // Filter and sort results by similarity score
            return userScores.entrySet().stream()
                    .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                    .sorted(Map.Entry.<BasicUserInfoDb, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }, FXCollections.observableArrayList());
    }

    /**
     * Gets all users from the database.
     */
    public static List<User> getAllUsers() {
        return executeTransactionWithResult(conn -> {
            List<User> usersList = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_GET_ALL_USERS);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    usersList.add(createUserFromResultSet(rs));
                }
            }
            return usersList;
        }, new ArrayList<>());
    }

    /**
     * Updates user information in the database using Utils transaction management.
     */
    public static boolean updateUserToDb(UserFormController.BasicUserInfo basicUserInfo) {
        if (basicUserInfo == null) {
            logger.error("❌ Null user information");
            return false;
        }

        return executeSimpleTransaction(conn -> {
                    try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_USER)) {
                        pstmt.setString(1, basicUserInfo.name());
                        pstmt.setString(2, basicUserInfo.email());
                        pstmt.setInt(3, basicUserInfo.typeUserId());
                        pstmt.setString(4, basicUserInfo.phoneNumber());
                        pstmt.setString(5, basicUserInfo.id());

                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected == 0) {
                            logger.warn("⚠️ No user found with ID {}", basicUserInfo.id());
                            throw new SQLException("No user found with the provided ID");
                        }
                    }
                },
                "✅ User with ID " + basicUserInfo.id() + " updated successfully",
                "❌ Error updating user");
    }

    /**
     * Deletes a user from the database by their ID using Utils transaction management.
     */
    public static boolean deleteUserToDb(String id) {
        if (id == null || id.isEmpty()) {
            logger.error("❌ Invalid user ID");
            return false;
        }

        return executeSimpleTransaction(conn -> {
                    try (PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_USER)) {
                        pstmt.setString(1, id);
                        int rowsAffected = pstmt.executeUpdate();

                        if (rowsAffected == 0) {
                            logger.warn("⚠️ No user found with ID {}", id);
                            throw new SQLException("No user found with the provided ID");
                        }
                    }
                },
                "✅ User with ID " + id + " deleted successfully",
                "❌ Error deleting user");
    }

    /**
     * Basic information about a user in the database.
     */
    public record BasicUserInfoDb(String id, String email, String name, int typeUserId, LocalDate createAt, String phoneNumber) {}
}