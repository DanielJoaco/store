package com.danieljoaco.storeapp.db;

import com.danieljoaco.storeapp.products.Products;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    // Obtener un producto por ID
    public Products getProductByRef(String ref) {
        String sql = """
                    SELECT
                        p.name,
                        p.ref,
                        p.cost,
                        p.price,
                        p.stock,
                        p.bill,
                        p.date,
                        c.name AS category,  -- Obtener la categoría
                        s.name AS subcategory -- Obtener la subcategoría
                    FROM products p
                    LEFT JOIN subcategories s ON p.subcategory_id = s.id
                    LEFT JOIN categories c ON s.category_id = c.id
                    WHERE p.ref = ?
                """;

        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ref);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Products(
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
            System.out.println("Error al obtener el producto: " + e.getMessage());
        }
        return null;  // Retorna null si no se encuentra el producto
    }

    // Listar todos los productos
    public List<Products> getAllProducts() {
        String sql = """
                SELECT
                    p.name,
                    p.ref,
                    p.cost,
                    p.price,
                    p.stock,
                    p.bill,
                    p.date,
                    c.name AS category,
                    s.name AS subcategory
                FROM products p
                LEFT JOIN subcategories s ON p.subcategory_id = s.id
                LEFT JOIN categories c ON s.category_id = c.id
            """;
        List<Products> productsList = new ArrayList<>();
        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Products product = new Products(
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
        }
        return productsList;
    }

    // Actualizar la información de un producto
    public void updateProduct(Products product) {
        String sql = "UPDATE products SET name = ?, price = ?, quantity = ?, category = ?, subcategory = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setString(4, product.getCategory());
            pstmt.setString(5, product.getSubCategory());
            pstmt.setString(6, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar el producto: " + e.getMessage());
        }
    }

    // Eliminar un producto por ID
    public void deleteProduct(String id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar el producto: " + e.getMessage());
        }
    }
}
