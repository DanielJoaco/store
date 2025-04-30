package com.danieljoaco.storeapp.db;

import com.danieljoaco.storeapp.product.ProductInfo;
import com.danieljoaco.storeapp.product.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsDao {

    /**
     * Adds a new product to the database.
     *
     * @param productData The product data to be added.
     */
    public static void addProduct(Product productData) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            long categoryId = getOrCreateCategory(conn, productData.getCategory());

            long subcategoryId = getOrCreateSubcategory(conn, productData.getSubCategory(), categoryId);

            String sqlRef = "INSERT OR IGNORE INTO product_references (ref, name, brand, description, subcategory_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtRef = conn.prepareStatement(sqlRef)) {
                pstmtRef.setString(1, productData.getRef());
                pstmtRef.setString(2, productData.getName());
                pstmtRef.setString(3, productData.getBrand());
                pstmtRef.setString(4, productData.getDescription());
                pstmtRef.setLong(5, subcategoryId);
                pstmtRef.executeUpdate();
            }

            String sqlProd = "INSERT INTO products (id, product_ref, cost, price, stock, bill, date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtProd = conn.prepareStatement(sqlProd)) {

                pstmtProd.setString(1, productData.getId());
                pstmtProd.setString(2, productData.getRef());
                pstmtProd.setDouble(3, productData.getCost());
                pstmtProd.setDouble(4, productData.getPrice());
                pstmtProd.setInt(5, productData.getStock());
                pstmtProd.setString(6, productData.getBill());
                pstmtProd.setDate(7, Date.valueOf(productData.getDate()));
                pstmtProd.executeUpdate();
            }

            conn.commit();
            System.out.println("Stock product/input created successfully for REF: " + productData.getRef());

        } catch (SQLException e) {
            System.err.println("Error when creating the product: " + e.getMessage());
            e.printStackTrace(); // Imprime más detalles del error
            try {
                if (conn != null) {
                    conn.rollback(); // Revertir transacción en caso de error
                    System.err.println("Reversal transaction.");
                }
            } catch (SQLException ex) {
                System.err.println("Error during the rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit); // Restaurar modo autocommit
                    conn.close(); // Cerrar conexión
                }
            } catch (SQLException ex) {
                System.err.println("Error when closing the connection: " + ex.getMessage());
            }
        }
    }

    /**
     * Obtains or creates a category in the database.
     *
     * @param conn         The connection to the database.
     * @param categoryName The name of the category to obtain or create.
     * @return The ID of the category.
     * @throws SQLException If there is an error accessing the database.
     */
    private static long getOrCreateCategory(Connection conn, String categoryName) throws SQLException {
        String selectSql = "SELECT id FROM categories WHERE name = ?";
        String insertSql = "INSERT INTO categories (name) VALUES (?)";
        return getOrCreateId(conn, selectSql, insertSql, categoryName);
    }

    /**
     * Obtains or creates a subcategory in the database.
     *
     * @param conn          The connection to the database.
     * @param subcategoryName The name of the subcategory to obtain or create.
     * @param categoryId    The ID of the category to which the subcategory belongs.
     * @return The ID of the subcategory.
     * @throws SQLException If there is an error accessing the database.
     */
    private static long getOrCreateSubcategory(Connection conn, String subcategoryName, long categoryId) throws SQLException {
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
        if (id == -1) throw new SQLException("The ID of the subcategory could not be obtained or created.");
        return id;
    }

    /**
     * Obtains or creates an ID in the database.
     *
     * @param conn        The connection to the database.
     * @param selectSql   The SQL query to select the ID.
     * @param insertSql   The SQL query to insert a new ID.
     * @param value       The value to be used for the ID.
     * @return The ID of the entity.
     * @throws SQLException If there is an error accessing the database.
     */
    private static long getOrCreateId(Connection conn, String selectSql, String insertSql, String value) throws SQLException {
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
        if (id == -1) throw new SQLException("The ID could not be obtained or created for [" + value + "].");
        return id;
    }

    /**
     * Searches for product references in the database based on a query.
     *
     * @param query The search query.
     * @return A list of matching product references.
     */
    public static ObservableList<ProductInfo> searchProductReferences(String query) {
        ObservableList<ProductInfo> results = FXCollections.observableArrayList();
        String searchPattern = "%" + query.replace("_", "\\_").replace("%", "\\%") + "%";

        String sql = """
            SELECT DISTINCT
                pr.ref,
                pr.name,
                pr.brand,
                c.name AS category,
                s.name AS subcategory,
                pr.description
            FROM product_references pr
            LEFT JOIN subcategories s ON pr.subcategory_id = s.id
            LEFT JOIN categories c ON s.category_id = c.id
            LEFT JOIN products p ON pr.ref = p.product_ref
            WHERE pr.ref LIKE ? ESCAPE '\\'
            OR pr.name LIKE ? ESCAPE '\\'
            OR pr.brand LIKE ? ESCAPE '\\'
            """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            Map<ProductInfo, Integer> referenceScores = new HashMap<>();

            while (rs.next()) {
                ProductInfo reference = new ProductInfo(
                        rs.getString("ref"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getString("category"),
                        rs.getString("subcategory"),
                        rs.getString("description")
                );

                // Calcular puntuación de similitud
                int nameScore = calculateSimilarityScore(query, reference.getName());
                int refScore = calculateSimilarityScore(query, reference.getRef());
                int brandScore = calculateSimilarityScore(query, reference.getBrand());
                int maxScore = Math.max(Math.max(nameScore, refScore), brandScore);

                referenceScores.put(reference, maxScore);
            }

            // Filtrar y ordenar resultados por puntuación
            referenceScores.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 80)
                    .sorted(Map.Entry.<ProductInfo, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .forEach(results::add);

        } catch (SQLException e) {
            System.out.println("Error looking for products: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Calculates the similarity score between two strings using the Levenshtein distance.
     *
     * @param search The search string.
     * @param target The target string.
     * @return A similarity score between 0 and 100.
     */
    public static int calculateSimilarityScore(String search, String target) {
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

    /**
     * Obtains all products from the database.
     *
     * @return A list of all products.
     */
    public static List<Product> getAllProducts() {
        String sql = """
            SELECT
                pr.name,
                pr.brand,
                pr.description,
                p.product_ref AS ref,
                p.id,
                p.cost,
                p.price,
                p.stock,
                p.bill,
                p.date,
                c.name AS category,
                s.name AS subcategory
            FROM products p
            JOIN product_references pr ON p.product_ref = pr.ref
            LEFT JOIN subcategories s ON pr.subcategory_id = s.id
            LEFT JOIN categories c ON s.category_id = c.id
        """;

        List<Product> productsList = new ArrayList<>();
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getString("ref"),
                        rs.getDouble("cost"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("bill"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("category"),
                        rs.getString("subcategory"),
                        rs.getString("description")
                );
                productsList.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Error obtaining the products: " + e.getMessage());
            e.printStackTrace();
        }
        return productsList;
    }

    /**
     * Updates a product reference in the database.
     */
    public static void updateProductReference(ProductInfo productReference) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            long categoryId = getOrCreateCategory(conn, productReference.getCategory());

            long subcategoryId = getOrCreateSubcategory(conn, productReference.getSubcategory(), categoryId);

            String sql = "UPDATE product_references SET name = ?, brand = ?, description = ?, subcategory_id = ? WHERE ref = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, productReference.getName());
                pstmt.setString(2, productReference.getBrand());
                pstmt.setString(3, productReference.getDescription());
                pstmt.setLong(4, subcategoryId);
                pstmt.setString(5, productReference.getRef());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Successfully updated product reference:" + productReference.getRef());
                } else {
                    System.out.println("The product reference was not found:" + productReference.getRef());
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Error when updating the product reference:" + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Reversal transaction.");
                }
            } catch (SQLException ex) {
                System.err.println("Error during the rollback:" + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error closing the connection:" + ex.getMessage());
            }
        }
    }

    /**
     * Updates a product entry in the database.
     *
     * @param product The product to be updated.
     */
    public static void updateProductEntry(Product product) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            long categoryId = getOrCreateCategory(conn, product.getCategory());
            long subcategoryId = getOrCreateSubcategory(conn, product.getSubCategory(), categoryId);

            String updateRef = "UPDATE product_references SET name = ?, brand = ?, description = ?, subcategory_id = ? WHERE ref = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRef)) {
                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getBrand());
                pstmt.setString(3, product.getDescription());
                pstmt.setLong(4, subcategoryId);
                pstmt.setString(5, product.getRef());
                pstmt.executeUpdate();
            }

            String updateProd = "UPDATE products SET cost = ?, price = ?, stock = ?, bill = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateProd)) {
                pstmt.setDouble(1, product.getCost());
                pstmt.setDouble(2, product.getPrice());
                pstmt.setInt(3, product.getStock());
                pstmt.setString(4, product.getBill());
                pstmt.setString(5, product.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Successfully updated product:" + product.getRef());
        } catch (SQLException e) {
            System.out.println("Error when updating the product:" + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Reversal transaction.");
                }
            } catch (SQLException ex) {
                System.err.println("Error during the rollback:" + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error closing the connection:" + ex.getMessage());
            }
        }
    }


    public static void deleteProductEntryToDb(String id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
                conn.commit();
                System.out.println("Successfully eliminated product entry.");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error eliminating the product:" + e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error in the database operation:" + e.getMessage());
        }
    }


    /**
     * Deletes a product from the database based on its reference.
     * Warning: This method deletes all entries related to the product in the database.
     * @param ref The reference of the product to be deleted.
     */
    public static void deleteProductToDb(String ref) {
        String sql = "DELETE FROM product_references WHERE ref = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setString(1, ref);
                pstmt.executeUpdate();
                conn.commit();
                System.out.println("Successfully eliminated product:" + ref);
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error eliminating the product:" + e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error in the database operation:" + e.getMessage());
        }
    }
}
