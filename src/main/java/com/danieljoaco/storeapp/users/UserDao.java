package com.danieljoaco.storeapp.users;

import com.danieljoaco.storeapp.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

        String checkSql  = "SELECT COUNT(*) FROM users WHERE id = ? OR email = ?";
        String insertSql = "INSERT INTO users (id, name, email, password, type_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.connectUsers()) {
            // 1) Comprueba duplicados
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


    public static ResultSet findUserById(String id) throws SQLException  {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = DatabaseManager.connectUsers();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        return pstmt.executeQuery();
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
                    String id       = rs.getString("id");
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

    public static List<Users> getAllUsers(){
        List<Users> usersList = new ArrayList<>();
        try{
            String sql = "SELECT * FROM users";
            Connection conn = DatabaseManager.connectUsers();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();


            while (rs.next()) {
                String id       = rs.getString("id");
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
}
