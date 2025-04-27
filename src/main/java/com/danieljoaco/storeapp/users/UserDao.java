package com.danieljoaco.storeapp.users;

import com.danieljoaco.storeapp.db.DatabaseManager;
import com.danieljoaco.storeapp.menu.signUp.UserFormController;
import com.danieljoaco.storeapp.products.ProductReference;
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

import static com.danieljoaco.storeapp.db.ProductsDao.calculateSimilarityScore;

public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    /**
     * Save a user in the database.
     * Validates if the user already exists by checking the ID and email.
     * @param user User to save.
     */
    public static void saveUser(Users user) {
        int userType;
        switch (user) {
            case Admin admin -> userType = 1;
            case SupportAgent supportAgent -> userType = 2;
            case Customer customer -> userType = 3;
            case null, default -> {
                logger.error("❌ User type of user.");
                throw new IllegalArgumentException("User type of user.");
            }
        }

        String checkSql  = "SELECT COUNT(*) FROM users WHERE id_user = ? OR email = ?";
        String insertSql = "INSERT INTO users (id_user, name, email, password, type_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.connectUsers()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getId());
                checkStmt.setString(2, user.getEmail());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        logger.warn("⚠️ Error: The ID or the email are already registered.");
                        return;
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getPasswordHash());
                pstmt.setInt(5, userType);
                pstmt.setDate(6, Date.valueOf(user.getCreatedAt()));

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    logger.info("✅ Saved user: {}", user.getEmail());
                } else {
                    logger.warn("⚠️ The user was not inserted.");
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Error when saving user: {}", e.getMessage());
        }
    }

    /**
     * Verifies if an admin user exists in the database.
     * @return true if an admin user exists, false otherwise.
     */
    public static boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE type_id = 1";
        try (Connection conn = DatabaseManager.connectUsers();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("❌ Error verifying admin: {}", e.getMessage());
        }
        return false;
    }


    /**
     * Finds a user by their email address.
     * @param email the email address of the user to find.
     * @return the user object if found, null otherwise.
     */
    public static Users findUserByEmail(String email) {

        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.connectUsers();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);


            try (ResultSet rs = pstmt.executeQuery()) {
                
                if (rs.next()) {
                    String id       = rs.getString("id_user");
                    String name     = rs.getString("name");
                    String pwdHash  = rs.getString("password");
                    int typeUser = rs.getInt("type_id");
                    LocalDate createdAt = rs.getDate("created_at").toLocalDate();

                    return switch (typeUser) {
                        case 1  -> Admin.createAdminFromDb(id, email, pwdHash, name, createdAt);
                        case 2  -> SupportAgent.createAgentFromDb(id, email, pwdHash, name, createdAt);
                        case 3  -> new Customer(id, email, pwdHash, name, createdAt);
                        default -> throw new IllegalStateException("Unknown type: " + typeUser);
                    };
                }
            }
        } catch (SQLException e) {
            logger.error("❌ ERROR When looking for a user: {}", e.getMessage());
        }
        return null;
    }

    public static ObservableList<BasicUserInfoDb> findUser(String query) {
        String searchPattern = "%" + query.replace("_", "\\_").replace("%", "\\%") + "%";
        ObservableList<BasicUserInfoDb> results = FXCollections.observableArrayList();
        String sql = """
                    SELECT DISTINCT
                        u.user_id,
                        u.name,
                        u.email,
                        u.type_id,
                        u.created_at
                    FROM users u
                    WHERE u.email LIKE ? OR u.name LIKE ? OR u.user_id LIKE ?
                    """;
        try (Connection conn = DatabaseManager.connectUsers();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            Map<BasicUserInfoDb, Integer> userScore = new HashMap<>();

            while (rs.next()){
                BasicUserInfoDb userInfo = new BasicUserInfoDb(
                        rs.getString("user_id"),
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getInt("type_id"),
                        rs.getDate("created_at").toLocalDate()
                );

                int user_id_score = calculateSimilarityScore(query, userInfo.id());
                int name_score = calculateSimilarityScore(query, userInfo.name());
                int email_score = calculateSimilarityScore(query, userInfo.email());
                int maxScore = Math.max(Math.max(user_id_score, name_score), email_score);

                userScore.put(userInfo, maxScore);
            }

            userScore.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 80)
                    .sorted(Map.Entry.<BasicUserInfoDb, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .forEach(results::add);

        } catch (SQLException e) {
            logger.error("❌ ERROR When looking for a user: {}", e.getMessage());
        }

        return results;

    }


    public static List<Users> getAllUsers(){
        List<Users> usersList = new ArrayList<>();
        try{
            String sql = "SELECT * FROM users";
            Connection conn = DatabaseManager.connectUsers();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();


            while (rs.next()) {
                String id       = rs.getString("id_user");
                String name     = rs.getString("name");
                String email    = rs.getString("email");
                String pwdHash  = rs.getString("password");
                int typeUser    = rs.getInt("type_id");
                LocalDate createdAt = rs.getDate("created_at").toLocalDate();

                Users user = switch (typeUser) {
                    case 1  -> Admin.createAdminFromDb(id, email, pwdHash, name, createdAt);
                    case 2  -> SupportAgent.createAgentFromDb(id, email, pwdHash, name, createdAt);
                    case 3  -> new Customer(id, email, pwdHash, name, createdAt);
                    default -> throw new IllegalStateException("Unknown type: " + typeUser);
                };
                usersList.add(user);
            }
        } catch (SQLException e) {
            logger.error("❌ ERROR When looking for all users: {}", e.getMessage());
        }

        return usersList;
    }

    public static void updateUserToDb(UserFormController.BasicUserInfo basicUserInfo){
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connectUsers();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sql = "UPDATE users SET id_user = ?, name = ?, email = ?, type_id = ? WHERE id_user = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, basicUserInfo.id());
                pstmt.setString(2, basicUserInfo.name());
                pstmt.setString(3, basicUserInfo.email());
                pstmt.setInt(4, basicUserInfo.typeUserId());
                pstmt.setString(5, basicUserInfo.id());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    logger.info("✅ User with ID {} updated successfully.", basicUserInfo.id());
                    conn.commit();
                } else {
                    logger.warn("⚠️ No user found with ID {}.", basicUserInfo.id());
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ Error updating user: {}", e.getMessage());
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void deleteUserToDb(String id) {
        String sql = "DELETE FROM users WHERE id_user = ?";
        try (Connection conn = DatabaseManager.connectUsers();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("✅ User with ID {} deleted successfully.", id);
            } else {
                logger.warn("⚠️ No user found with ID {}.", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Error deleting user: {}", e.getMessage());
        }
    }

    /**
     * Validates the basic user input
     * @param id The id of the user
     * @param email The email of the user
     * @param name The name of the user
     * @param typeUserId The type of user (1: Admin, 2: Support Agent, 3: Customer)
     * @param createAt The date of creation of the user
     */
    public record BasicUserInfoDb(String id, String email, String name, int typeUserId, LocalDate createAt) {}
}
