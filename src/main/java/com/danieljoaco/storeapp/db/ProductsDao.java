package com.danieljoaco.storeapp.db;

import com.danieljoaco.storeapp.products.Products;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductsDao {

    // Crear un nuevo producto en la base de datos
    public static void createProduct(Products product) {
        String sql = "INSERT INTO products(id, name, price, quantity, category, subcategory) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getId());  // Asumiendo que getId() genera un UUID único
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getSubCategory());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al crear el producto: " + e.getMessage());
        }
    }

    // Obtener un producto por ID
    public Products getProductById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Products(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getString("subcategory"),
                        rs.getString("admin")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener el producto: " + e.getMessage());
        }
        return null;  // Retorna null si no se encuentra el producto
    }

    // Listar todos los productos
    public List<Products> getAllProducts() {
        String sql = "SELECT * FROM products";
        List<Products> productsList = new ArrayList<>();
        try (Connection conn = DatabaseManager.connectProducts(); // Usamos connectProducts()
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Products product = new Products(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
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
            pstmt.setInt(3, product.getQuantity());
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
