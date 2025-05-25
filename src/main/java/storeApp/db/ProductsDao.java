package storeApp.db;

import storeApp.product.ProductInfo;
import storeApp.product.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static storeApp.db.Utils.*;

public class ProductsDao {

    /**
     * Adds a new product to the database.
     *
     * @param productData The product data to be added.
     */
    public static void addProduct(Product productData) {
        executeSimpleTransaction(
                conn -> performAddProduct(conn, productData),
                "Stock product/input created successfully for REF: " + productData.getRef(),
                "Error when creating the product"
        );
    }

    /**
     * Lógica específica para agregar producto
     */
    private static void performAddProduct(Connection conn, Product productData) throws SQLException {
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
    }

    /**
     * Searches for product references in the database based on a query.
     *
     * @param query The search query.
     * @return A list of matching product references.
     */
    public static ObservableList<ProductInfo> searchProductByQuery(String query) {
        return executeTransactionWithResult(
                conn -> performSearchProductByQuery(conn, query),
                FXCollections.observableArrayList()
        );
    }

    /**
     * Lógica específica para buscar productos
     */
    private static ObservableList<ProductInfo> performSearchProductByQuery(Connection conn, String query) throws SQLException {
        ObservableList<ProductInfo> results = FXCollections.observableArrayList();
        String searchPattern = "%" + query.replace("_", "\\_").replace("%", "\\%") + "%";

        String sql = """
        SELECT DISTINCT
            pr.ref,
            pr.name,
            pr.brand,
            c.name AS category,
            s.name AS subcategory,
            pr.description,
            p.id AS product_id
        FROM product_references pr
        LEFT JOIN subcategories s ON pr.subcategory_id = s.id
        LEFT JOIN categories c ON s.category_id = c.id
        LEFT JOIN products p ON pr.ref = p.product_ref
        WHERE pr.ref LIKE ? ESCAPE '\\'
        OR pr.name LIKE ? ESCAPE '\\'
        OR pr.brand LIKE ? ESCAPE '\\'
        OR p.id LIKE ? ESCAPE '\\'
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);

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

                // Calcular puntuación de similitud usando Utils
                int nameScore = calculateSimilarityScore(query, reference.getName());
                int refScore = calculateSimilarityScore(query, reference.getRef());
                int brandScore = calculateSimilarityScore(query, reference.getBrand());
                int idScore = calculateSimilarityScore(query, rs.getString("product_id"));
                int maxScore = Math.max(Math.max(Math.max(nameScore, refScore), brandScore), idScore);

                referenceScores.put(reference, maxScore);
            }

            // Filtrar y ordenar resultados por puntuación
            referenceScores.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 80)
                    .sorted(Map.Entry.<ProductInfo, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .forEach(results::add);
        }

        return results;
    }

    /**
     * Obtains all products from the database.
     *
     * @return A list of all products.
     */
    public static List<Product> getAllProducts() {
        return executeTransactionWithResult(
                ProductsDao::performGetAllProducts,
                new ArrayList<>()
        );
    }

    /**
     * Lógica específica para obtener todos los productos
     */
    private static List<Product> performGetAllProducts(Connection conn) throws SQLException {
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
        try (Statement stmt = conn.createStatement();
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
        }
        return productsList;
    }

    public static List<ProductViewInfo> getAllProductsView() {
        return executeTransactionWithResult(
                ProductsDao::performGetAllProductsView,
                new ArrayList<>()
        );
    }

    /**
     * Lógica específica para obtener vista de productos
     */
    private static List<ProductViewInfo> performGetAllProductsView(Connection conn) throws SQLException {
        String sql = """
            SELECT
                pr.name,
                pr.brand,
                pr.description,
                p.id,
                p.product_ref AS ref,
                p.price,
                SUM(p.stock) AS stock,
                c.name AS category,
                s.name AS subcategory
            FROM products p
            JOIN product_references pr ON p.product_ref = pr.ref
            LEFT JOIN subcategories s ON pr.subcategory_id = s.id
            LEFT JOIN categories c ON s.category_id = c.id
            GROUP BY
                p.product_ref,
                p.price,
                p.id,
                pr.name,
                pr.brand,
                pr.description,
                c.name,
                s.name
            ORDER BY pr.name
            """;

        List<ProductViewInfo> productsViewList = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProductViewInfo productView = new ProductViewInfo(
                        rs.getString("id"),
                        rs.getString("ref"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getString("category"),
                        rs.getString("subcategory"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        "",
                        rs.getString("description")
                );
                productsViewList.add(productView);
            }
        }
        return productsViewList;
    }

    /**
     * Updates a product reference in the database.
     */
    public static void updateProductReference(ProductInfo productReference) {
        executeSimpleTransaction(
                conn -> performUpdateProductReference(conn, productReference),
                "Successfully updated product reference: " + productReference.getRef(),
                "Error when updating the product reference"
        );
    }

    /**
     * Lógica específica para actualizar referencia de producto
     */
    private static void performUpdateProductReference(Connection conn, ProductInfo productReference) throws SQLException {
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
            if (rowsAffected == 0) {
                System.out.println("The product reference was not found: " + productReference.getRef());
            }
        }
    }

    /**
     * Updates a product entry in the database.
     *
     * @param product The product to be updated.
     */
    public static void updateProductEntry(Product product) {
        executeSimpleTransaction(
                conn -> performUpdateProductEntry(conn, product),
                "Successfully updated product: " + product.getRef(),
                "Error when updating the product"
        );
    }

    /**
     * Lógica específica para actualizar entrada de producto
     */
    private static void performUpdateProductEntry(Connection conn, Product product) throws SQLException {
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
    }

    public static void deleteProductEntryToDb(String id) {
        executeSimpleTransaction(
                conn -> performDeleteProductEntry(conn, id),
                "Successfully eliminated product entry.",
                "Error eliminating the product"
        );
    }

    /**
     * Lógica específica para eliminar entrada de producto
     */
    private static void performDeleteProductEntry(Connection conn, String id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a product from the database based on its reference.
     * Warning: This method deletes all entries related to the product in the database.
     * @param ref The reference of the product to be deleted.
     */
    public static void deleteProductToDb(String ref) {
        executeSimpleTransaction(
                conn -> performDeleteProduct(conn, ref),
                "Successfully eliminated product: " + ref,
                "Error eliminating the product"
        );
    }

    /**
     * Lógica específica para eliminar producto completo
     */
    private static void performDeleteProduct(Connection conn, String ref) throws SQLException {
        String sql = "DELETE FROM product_references WHERE ref = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ref);
            pstmt.executeUpdate();
        }
    }

    public record ProductViewInfo(
            String id,
            String ref,
            String name,
            String brand,
            String category,
            String subcategory,
            int stock,
            double price,
            String image,
            String description)
    {}
}