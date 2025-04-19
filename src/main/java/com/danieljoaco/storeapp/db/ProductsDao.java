package com.danieljoaco.storeapp.db;

import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.products.Products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsDao {

    // Crear un nuevo producto en la base de datos
    public static void addProduct(Products productData) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connectProducts();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Iniciar transacción

            // --- PASO 1: Obtener o crear Category ID ---
            long categoryId = getOrCreateCategory(conn, productData.getCategory());

            // --- PASO 2: Obtener o crear Subcategory ID ---
            long subcategoryId = getOrCreateSubcategory(conn, productData.getSubCategory(), categoryId);

            // --- PASO 3: Crear o verificar Product Reference ---
            // Usamos INSERT OR IGNORE (SQLite) para simplificar: si ya existe la ref, no hace nada.
            // Si tuvieras que actualizar el nombre o subcategoría si la ref ya existe, la lógica sería más compleja.
            String sqlRef = "INSERT OR IGNORE INTO product_references (ref, name, subcategory_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmtRef = conn.prepareStatement(sqlRef)) {
                pstmtRef.setString(1, productData.getRef());
                pstmtRef.setString(2, productData.getName());
                pstmtRef.setLong(3, subcategoryId);
                pstmtRef.executeUpdate();
            }

            // --- PASO 4: Insertar la entrada de stock en Products ---
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
            System.out.println("Producto/Entrada de stock creado exitosamente para ref: " + productData.getRef());

        } catch (SQLException e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
            e.printStackTrace(); // Imprime más detalles del error
            try {
                if (conn != null) {
                    conn.rollback(); // Revertir transacción en caso de error
                    System.err.println("Transacción revertida.");
                }
            } catch (SQLException ex) {
                System.err.println("Error durante el rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit); // Restaurar modo autocommit
                    conn.close(); // Cerrar conexión
                }
            } catch (SQLException ex) {
                System.err.println("Error al cerrar la conexión: " + ex.getMessage());
            }
        }
    }

    private static long getOrCreateCategory(Connection conn, String categoryName) throws SQLException {
        String selectSql = "SELECT id FROM categories WHERE name = ?";
        String insertSql = "INSERT INTO categories (name) VALUES (?)";
        return getOrCreateId(conn, selectSql, insertSql, categoryName);
    }

    private static long getOrCreateSubcategory(Connection conn, String subcategoryName, long categoryId) throws SQLException {
        String selectSql = "SELECT id FROM subcategories WHERE name = ? AND category_id = ?";
        String insertSql = "INSERT INTO subcategories (name, category_id) VALUES (?, ?)";

        long id = -1;
        // Intentar seleccionar
        try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
            pstmtSelect.setString(1, subcategoryName);
            pstmtSelect.setLong(2, categoryId);
            ResultSet rs = pstmtSelect.executeQuery();
            if (rs.next()) {
                id = rs.getLong("id");
            }
        }

        // Si no existe, insertar
        if (id == -1) {
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setString(1, subcategoryName);
                pstmtInsert.setLong(2, categoryId);
                pstmtInsert.executeUpdate();
                ResultSet generatedKeys = pstmtInsert.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Fallo al crear subcategoría, no se obtuvo ID.");
                }
            }
        }
        if (id == -1) throw new SQLException("No se pudo obtener o crear el ID de la subcategoría.");
        return id;
    }

    // Función genérica para obtener o crear ID basado en un valor único (como el nombre de categoría)
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
                    throw new SQLException("Fallo al crear la entidad [" + value + "], no se obtuvo ID.");
                }
            }
        }
        if (id == -1) throw new SQLException("No se pudo obtener o crear el ID para [" + value + "].");
        return id;
    }

    public static Products getProductEntryById(String id) {
        Products product = null;

        String sql = """
            SELECT
                pr.name,
                p.id,
                p.product_ref AS ref,
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
            WHERE p.id = ?
            """;

        try (Connection conn = DatabaseManager.connectProducts();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                product = new Products(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("ref"),
                        rs.getDouble("cost"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("bill"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("category"),
                        rs.getString("subcategory")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar producto por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return product;
    }

    public static ObservableList<ProductReference> searchProductReferences(String query) {
        ObservableList<ProductReference> results = FXCollections.observableArrayList();
        String searchPattern = "%" + query.replace("_", "\\_").replace("%", "\\%") + "%";

        String sql = """
            SELECT DISTINCT
                pr.ref,
                pr.name,
                c.name AS category,
                s.name AS subcategory
            FROM product_references pr
            LEFT JOIN subcategories s ON pr.subcategory_id = s.id
            LEFT JOIN categories c ON s.category_id = c.id
            LEFT JOIN products p ON pr.ref = p.product_ref
            WHERE pr.ref LIKE ? ESCAPE '\\'
            OR pr.name LIKE ? ESCAPE '\\'
            """;

        try (Connection conn = DatabaseManager.connectProducts();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            Map<ProductReference, Integer> referenceScores = new HashMap<>();

            while (rs.next()) {
                ProductReference reference = new ProductReference(
                        rs.getString("ref"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("subcategory")
                );

                // Calcular puntuación de similitud
                int nameScore = calculateSimilarityScore(query, reference.getName());
                int refScore = calculateSimilarityScore(query, reference.getRef());
                int maxScore = Math.max(nameScore, refScore);

                referenceScores.put(reference, maxScore);
            }

            // Filtrar y ordenar resultados por puntuación
            referenceScores.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 60) // Umbral de similitud menos estricto
                    .sorted(Map.Entry.<ProductReference, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .forEach(results::add);

        } catch (SQLException e) {
            System.out.println("Error al buscar referencias de productos: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    private static int calculateSimilarityScore(String search, String target) {
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

    // Listar todos los productos
    public static List<Products> getAllProducts() {
        String sql = """
            SELECT
                pr.name,           -- Get name from product_references table
                p.product_ref AS ref,  -- Get ref from the product table
                p.id,
                p.cost,
                p.price,
                p.stock,
                p.bill,
                p.date,
                c.name AS category,
                s.name AS subcategory
            FROM products p
            JOIN product_references pr ON p.product_ref = pr.ref  -- Join with product_references
            LEFT JOIN subcategories s ON pr.subcategory_id = s.id  -- Join subcategories with product_references
            LEFT JOIN categories c ON s.category_id = c.id
        """;
        List<Products> productsList = new ArrayList<>();
        try (Connection conn = DatabaseManager.connectProducts();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Products product = new Products(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("ref"),
                        rs.getDouble("cost"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("bill"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("category"),
                        rs.getString("subcategory")
                );
                productsList.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener los productos: " + e.getMessage());
            e.printStackTrace(); // Add this to get more details about the error
        }
        return productsList;
    }

    public static void updateProductReference(ProductReference productReference) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connectProducts();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Iniciar transacción

            // Paso 1: Obtener o crear ID de categoría
            long categoryId = getOrCreateCategory(conn, productReference.getCategory());

            // Paso 2: Obtener o crear ID de subcategoría
            long subcategoryId = getOrCreateSubcategory(conn, productReference.getSubcategory(), categoryId);

            // Paso 3: Actualizar la referencia del producto
            String sql = "UPDATE product_references SET name = ?, subcategory_id = ? WHERE ref = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, productReference.getName());
                pstmt.setLong(2, subcategoryId);
                pstmt.setString(3, productReference.getRef());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Referencia de producto actualizada exitosamente: " + productReference.getRef());
                } else {
                    System.out.println("No se encontró la referencia de producto: " + productReference.getRef());
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Error al actualizar la referencia de producto: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Transacción revertida.");
                }
            } catch (SQLException ex) {
                System.err.println("Error durante el rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error al cerrar la conexión: " + ex.getMessage());
            }
        }
    }

    // Actualizar la información de un producto
    public static void updateProductEntry(Products product) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseManager.connectProducts();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Start transaction

            // Update the product_references table (name and categories)
            long categoryId = getOrCreateCategory(conn, product.getCategory());
            long subcategoryId = getOrCreateSubcategory(conn, product.getSubCategory(), categoryId);

            String updateRef = "UPDATE product_references SET name = ?, subcategory_id = ? WHERE ref = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRef)) {
                pstmt.setString(1, product.getName());
                pstmt.setLong(2, subcategoryId);
                pstmt.setString(3, product.getRef());
                pstmt.executeUpdate();
            }

            // Update products table (price, stock, etc.)
            String updateProd = "UPDATE products SET cost = ?, price = ?, stock = ?, bill = ?, date = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateProd)) {
                pstmt.setDouble(1, product.getCost());
                pstmt.setDouble(2, product.getPrice());
                pstmt.setInt(3, product.getStock());
                pstmt.setString(4, product.getBill());
                pstmt.setDate(5, Date.valueOf(product.getDate()));
                pstmt.setString(6, product.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Producto actualizado exitosamente: " + product.getRef());
        } catch (SQLException e) {
            System.out.println("Error al actualizar el producto: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Transacción revertida.");
                }
            } catch (SQLException ex) {
                System.err.println("Error durante el rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error al cerrar la conexión: " + ex.getMessage());
            }
        }
    }

    // Eliminar un producto por ref

    public static void deleteProductToDb(String ref) {
        String sql = "DELETE FROM product_references WHERE ref = ?";

        try (Connection conn = DatabaseManager.connectProducts();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Iniciamos una transacción

            try {
                pstmt.setString(1, ref);
                pstmt.executeUpdate();
                conn.commit(); // Confirmamos la transacción
                System.out.println("Producto eliminado exitosamente: " + ref);
            } catch (SQLException e) {
                conn.rollback(); // Revertimos la transacción en caso de error
                System.out.println("Error al eliminar el producto: " + e.getMessage());
                throw e; // Re-lanzamos la excepción para manejarla en el nivel superior
            }
        } catch (SQLException e) {
            System.out.println("Error en la operación de base de datos: " + e.getMessage());
        }
    }
}
