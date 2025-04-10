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
            // Asegúrate de que el driver está disponible, el registro es automático
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("El driver JDBC de SQLite no está en el classpath", e);
            throw new RuntimeException("El driver JDBC de SQLite no está en el classpath", e);
        }
    }


    /**
     * Conecta a la base de datos de usuarios con cifrado.
     */
    public static Connection connectUsers() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL_USERS);

            // Aplicar las configuraciones de cifrado específicas
            applyEncryptionPragmas(conn);

            // Crear tabla en caso de que no exista
            createUsersTable(conn);

            return conn;
        } catch (SQLException e) {
            logger.error("Error al conectar a la base de datos de usuarios", e);
            throw e;
        }
    }

    /**
     * Aplica las configuraciones de cifrado a una conexión SQLite.
     */
    private static void applyEncryptionPragmas(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute(String.format("PRAGMA key = '%s';", KEY));
            stat.execute("PRAGMA cipher_page_size = 4096;");
            stat.execute("PRAGMA kdf_iter = 64000;");
        } catch (SQLException e) {
            logger.error("Error al aplicar las configuraciones de cifrado", e);
            throw e;
        }
    }

    /**
     * Crea la tabla de usuarios si no existe.
     */
    private static void createUsersTable(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
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
        }
    }

    /**
     * Conecta a la base de datos de productos.
     */
    public static Connection connectProducts() throws SQLException {
        logger.info("Conectando a la base de datos de productos...");
        try {
            Connection conn = DriverManager.getConnection(URL_PRODUCTS);

            // Crear tablas de productos si no existen
            createProductTables(conn);

            return conn;
        } catch (SQLException e) {
            logger.error("Error al conectar a la base de datos de productos", e);
            throw e;
        }
    }

    /**
     * Crea las tablas relacionadas con productos si no existen.
     */
    private static void createProductTables(Connection conn) throws SQLException {
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
            logger.error("Error al crear tablas de productos", e);
            throw e;
        }
    }
}