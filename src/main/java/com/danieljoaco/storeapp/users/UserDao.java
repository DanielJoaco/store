package com.danieljoaco.storeapp.users;

import com.danieljoaco.storeapp.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    // Método para guardar un usuario
    public static void saveUser(Users user) {
        String userType;
        if (user instanceof Admin) {
            userType = Users.UserType.ADMIN.name();
        } else if (user instanceof SupportAgent) {
            userType = Users.UserType.SUPPORT_AGENT.name();
        } else if (user instanceof Customer) {
            userType = Users.UserType.CUSTOMER.name();
        } else {
            logger.error("❌ Tipo de usuario no válido.");
            throw new IllegalArgumentException("Tipo de usuario no válido.");
        }

        String checkSql  = "SELECT COUNT(*) FROM users WHERE id = ? OR email = ?";
        String insertSql = "INSERT INTO users (id, name, email, password, typeUser) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.connectUsers()) {
            // 1) Comprueba duplicados
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getId());
                checkStmt.setString(2, user.getEmail());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        logger.warn("⚠️ Error: El ID o el email ya están registrados.");
                        return;
                    }
                }
            }

            // 2) Inserta el usuario
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getPasswordHash());
                pstmt.setString(5, userType);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    logger.info("✅ Usuario guardado: {}", user.getEmail());
                } else {
                    logger.warn("⚠️ No se insertó el usuario.");
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Error al guardar usuario: {}", e.getMessage());
        }
    }

    // Verificar si ya existe un Admin en la base de datos
    public static boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE typeUser = 'ADMIN'";
        try (Connection conn = DatabaseManager.connectUsers();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("❌ Error verificando admin: {}", e.getMessage());
        }
        return false;
    }

    // Obtener usuario por ID
    public static ResultSet findUserById(String id) throws SQLException  {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = DatabaseManager.connectUsers();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        return pstmt.executeQuery();
    }

    // Obtener usuario por Email
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
                    String typeUser = rs.getString("typeUser").toUpperCase();

                    return switch (typeUser) {
                        case "ADMIN"         -> Admin.createAdminFromDb(id, email, pwdHash, name);
                        case "SUPPORT_AGENT" -> SupportAgent.createAgentFromDb(id, email, pwdHash, name);
                        case "CUSTOMER"      -> new Customer(id, email, pwdHash, name);
                        default              -> throw new IllegalStateException("Unknown type: " + typeUser);
                    };
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error al buscar usuario: {}", e.getMessage());
        }
        return null;
    }
}
