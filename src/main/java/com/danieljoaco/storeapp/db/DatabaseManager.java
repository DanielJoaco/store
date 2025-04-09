package com.danieljoaco.storeapp.db;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private static final String URL_USERS = "jdbc:sqlite:encrypted_users.db";
    private static final String KEY = "MiClaveDeCifrado123";

    private static final String URL_PRODUCTS = "jdbc:sqlite:products.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            logger.info("Driver JDBC de SQLite cargado correctamente.");
        } catch (ClassNotFoundException e) {
            logger.error("No se pudo cargar el driver JDBC de SQLite", e);
            throw new RuntimeException("No se pudo cargar el driver JDBC de SQLite", e);
        }
    }

    public static Connection connectUsers() throws SQLException {
        logger.info("Conectando a la base de datos de usuarios...");
        Connection conn = DriverManager.getConnection(URL_USERS);
        try (Statement stat = conn.createStatement()) {
            stat.execute(String.format("PRAGMA key = '%s';", KEY));
            stat.execute("PRAGMA cipher_page_size = 4096;");
            stat.execute("PRAGMA kdf_iter = 64000;");
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
            logger.info("Tabla 'users' verificada/creada.");
        } catch (SQLException e) {
            logger.error("Error al conectar a la base de datos de usuarios", e);
            throw e;
        }
        return conn;
    }

    public static Connection connectProducts() throws SQLException {
        logger.info("Conectando a la base de datos de productos...");
        Connection conn = DriverManager.getConnection(URL_PRODUCTS);
        try (Statement stat = conn.createStatement()) {
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
            logger.info("Tablas de productos verificadas/creadas.");
        } catch (SQLException e) {
            logger.error("Error al conectar a la base de datos de productos", e);
            throw e;
        }
        return conn;
    }
}
