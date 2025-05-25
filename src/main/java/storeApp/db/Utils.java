package storeApp.db;

import storeApp.orders.*;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    // ===============================
    // MÉTODOS DE GESTIÓN DE TRANSACCIONES
    // ===============================

    /**
     * Ejecuta una transacción genérica con una orden
     */
    public static boolean executeOrderTransaction(Order order, OrderTransactionFunction function) {
        return executeTransactionWithResult(conn -> {
            function.execute(conn, order);
            return true;
        }, false);
    }

    /**
     * Ejecuta una transacción genérica que devuelve un resultado
     */
    public static <T> T executeTransactionWithResult(TransactionFunction<T> function, T defaultValue) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = function.execute(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            logger.error("Error executing database transaction", e);
            rollbackConnection(conn);
            return defaultValue;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Ejecuta una transacción simple con manejo de errores estándar
     */
    public static boolean executeSimpleTransaction(SimpleTransactionFunction function, String successMessage, String errorMessage) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            function.execute(conn);

            conn.commit();
            if (successMessage != null) {
                System.out.println(successMessage);
            }
            return true;
        } catch (SQLException e) {
            if (errorMessage != null) {
                System.err.println(errorMessage + ": " + e.getMessage());
            }
            e.printStackTrace();
            rollbackConnection(conn);
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS DE BASE DE DATOS
    // ===============================

    /**
     * Obtiene un ID por nombre de una tabla específica
     */
    public static int getIdByName(Connection conn, String tableName, String columnName, String value) throws SQLException {
        String sql = String.format("SELECT id FROM %s WHERE %s = ?", tableName, columnName);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    /**
     * Obtiene o crea un ID genérico en la base de datos
     */
    public static long getOrCreateId(Connection conn, String selectSql, String insertSql, String value) throws SQLException {
        long id = -1;

        // Intentar seleccionar
        try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
            pstmtSelect.setString(1, value);
            ResultSet rs = pstmtSelect.executeQuery();
            if (rs.next()) {
                id = rs.getLong("id");
            }
        }

        // Si no existe, insertar
        if (id == -1) {
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setString(1, value);
                pstmtInsert.executeUpdate();
                ResultSet generatedKeys = pstmtInsert.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Failure to create the entity [" + value + "], ID was not obtained.");
                }
            }
        }

        if (id == -1) {
            throw new SQLException("The ID could not be obtained or created for [" + value + "].");
        }
        return id;
    }

    /**
     * Obtiene o crea una categoría en la base de datos
     */
    public static long getOrCreateCategory(Connection conn, String categoryName) throws SQLException {
        String selectSql = "SELECT id FROM categories WHERE name = ?";
        String insertSql = "INSERT INTO categories (name) VALUES (?)";
        return getOrCreateId(conn, selectSql, insertSql, categoryName);
    }

    /**
     * Obtiene o crea una subcategoría en la base de datos
     */
    public static long getOrCreateSubcategory(Connection conn, String subcategoryName, long categoryId) throws SQLException {
        String selectSql = "SELECT id FROM subcategories WHERE name = ? AND category_id = ?";
        String insertSql = "INSERT INTO subcategories (name, category_id) VALUES (?, ?)";

        long id = -1;
        try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
            pstmtSelect.setString(1, subcategoryName);
            pstmtSelect.setLong(2, categoryId);
            ResultSet rs = pstmtSelect.executeQuery();
            if (rs.next()) {
                id = rs.getLong("id");
            }
        }

        if (id == -1) {
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setString(1, subcategoryName);
                pstmtInsert.setLong(2, categoryId);
                pstmtInsert.executeUpdate();
                ResultSet generatedKeys = pstmtInsert.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Failure when creating subcategory, ID was not obtained.");
                }
            }
        }

        if (id == -1) {
            throw new SQLException("The ID of the subcategory could not be obtained or created.");
        }
        return id;
    }

    // ===============================
    // MÉTODOS DE GESTIÓN DE CONEXIONES
    // ===============================

    /**
     * Realiza rollback de una conexión
     */
    public static void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback", rollbackEx);
                System.err.println("Error during the rollback: " + rollbackEx.getMessage());
            }
        }
    }

    /**
     * Cierra una conexión restaurando el autocommit original
     */
    public static void closeConnection(Connection conn, boolean originalAutoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(originalAutoCommit);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
                System.err.println("Error when closing the connection: " + e.getMessage());
            }
        }
    }

    // ===============================
    // MÉTODOS DE CÁLCULO
    // ===============================

    /**
     * Calcula la puntuación de similitud entre dos strings usando distancia de Levenshtein
     */
    public static int calculateSimilarityScore(String search, String target) {
        if (search == null || target == null) {
            return 0;
        }

        search = search.toLowerCase();
        target = target.toLowerCase();

        // Distancia de Levenshtein
        int[][] dp = new int[search.length() + 1][target.length() + 1];

        for (int i = 0; i <= search.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= target.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= search.length(); i++) {
            for (int j = 1; j <= target.length(); j++) {
                if (search.charAt(i - 1) == target.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
                }
            }
        }

        // Convertir la distancia en una puntuación de similitud (0-100)
        int maxLength = Math.max(search.length(), target.length());
        int distance = dp[search.length()][target.length()];
        return (int) ((1 - (double) distance / maxLength) * 100);
    }

    // ===============================
    // INTERFACES FUNCIONALES
    // ===============================

    @FunctionalInterface
    public interface TransactionFunction<T> {
        T execute(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface OrderTransactionFunction {
        void execute(Connection conn, Order order) throws SQLException;
    }

    @FunctionalInterface
    public interface SimpleTransactionFunction {
        void execute(Connection conn) throws SQLException;
    }
}