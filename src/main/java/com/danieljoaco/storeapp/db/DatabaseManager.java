package com.danieljoaco.storeapp.db;

import java.sql.*;

public class DatabaseManager {

    // Configuración para la base de datos cifrada de usuarios
    private static final String URL_USERS = "jdbc:sqlite:encrypted_users.db";
    private static final String KEY = "MiClaveDeCifrado123";  // Clave de cifrado (proteger más adelante)

    // Configuración para la base de datos de productos sin cifrado
    private static final String URL_PRODUCTS = "jdbc:sqlite:products.db";  // Base de datos no cifrada

    // Cargar el driver JDBC para SQLite
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver JDBC de SQLite", e);
        }
    }

    // Conectar a la base de datos cifrada de usuarios
    public static Connection connectUsers() throws SQLException {
        Connection conn = DriverManager.getConnection(URL_USERS);
        try (Statement stat = conn.createStatement()) {
            // Aplicar la clave de cifrado
            stat.execute(String.format("PRAGMA key = '%s';", KEY));
            stat.execute("PRAGMA cipher_page_size = 4096;");
            stat.execute("PRAGMA kdf_iter = 64000;");
            // Crear tabla 'users' si no existe
            stat.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    typeUser TEXT NOT NULL CHECK (typeUser IN ('CUSTOMER', 'ADMIN', 'SUPPORT_AGENT')),
                    name TEXT,
                    balance REAL DEFAULT 0.0
                );
            """);
            // Crear trigger para impedir cambios en 'typeUser'
            stat.execute("""
                CREATE TRIGGER IF NOT EXISTS prevent_type_user_change
                BEFORE UPDATE ON users
                FOR EACH ROW
                WHEN NEW.typeUser != OLD.typeUser
                BEGIN
                    SELECT RAISE(ABORT, 'Cannot change typeUser once set.');
                END;
            """);
        }
        return conn;
    }

    // Conectar a la base de datos de productos sin cifrado
    public static Connection connectProducts() throws SQLException {
        Connection conn = DriverManager.getConnection(URL_PRODUCTS);
        try (Statement stat = conn.createStatement()) {
            // Crear las tablas de productos si no existen
            stat.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                );
            """);

            stat.execute("""
                CREATE TABLE IF NOT EXISTS subcategories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category_id INTEGER,
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                );
            """);

            stat.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    quantity INTEGER NOT NULL,
                    subcategory_id INTEGER,
                    FOREIGN KEY (subcategory_id) REFERENCES subcategories(id)
                );
            """);

            stat.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id TEXT PRIMARY KEY,
                    product_id TEXT,
                    rating INTEGER NOT NULL,
                    comment TEXT,
                    customer_name TEXT,
                    date DATE,
                    FOREIGN KEY (product_id) REFERENCES products(id)
                );
            """);

        }
        return conn;
    }
}
