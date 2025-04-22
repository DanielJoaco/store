package com.danieljoaco.storeapp.db;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String URL_USERS = "jdbc:sqlite:encrypted_users.db";
    private static final String KEY = "MiClaveDeCifrado123";
    private static final String URL_PRODUCTS = "jdbc:sqlite:products.db";

    /**
     * Static block to load the SQLite JDBC driver.
     * This is necessary for the SQLite connection to work.
     */
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("SQLite's JDBC driver is not at Classpath", e);
            throw new RuntimeException("SQLite's JDBC driver is not at Classpath", e);
        }
    }

    /**
     * Connects to the user database and applies encryption settings.
     * @return a Connection object to the user database.
     * @throws SQLException if there is an error connecting to the database.
     */
    public static Connection connectUsers() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL_USERS);

            applyEncryptionPragmas(conn);

            createUsersTable(conn);

            return conn;
        } catch (SQLException e) {
            logger.error("Error connecting to the user database", e);
            throw e;
        }
    }

    /**
     * Applies encryption settings to the SQLite database.
     * @param conn the Connection object to the database.
     * @throws SQLException if there is an error applying the settings.
     */
    private static void applyEncryptionPragmas(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute(String.format("PRAGMA key = '%s';", KEY));
            stat.execute("PRAGMA cipher_page_size = 4096;");
            stat.execute("PRAGMA kdf_iter = 64000;");
        } catch (SQLException e) {
            logger.error("Error when applying encryption settings", e);
            throw e;
        }
    }

    /**
     * Creates the user tables if they do not exist.
     * @param conn the Connection object to the database.
     * @throws SQLException if there is an error creating the tables.
     */
    private static void createUsersTable(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute("""
            CREATE TABLE IF NOT EXISTS user_types (
                id INTEGER PRIMARY KEY,
                type_name TEXT UNIQUE NOT NULL
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                type_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                created_at DATE NOT NULL,
                balance REAL DEFAULT 0.0,
                FOREIGN KEY (type_id) REFERENCES user_types(id)
            );
        """);

            ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM user_types;");
            int count = rs.getInt(1);

            if (count == 0) {
                stat.execute("""
                INSERT INTO user_types (id, type_name) VALUES
                (1, 'ADMIN'),
                (2, 'SUPPORT_AGENT'),
                (3, 'CUSTOMER');
            """);
            }
        }
    }

    /**
     * Connects to the product database and creates the necessary tables.
     * @return a Connection object to the product database.
     * @throws SQLException if there is an error connecting to the database.
     */
    public static Connection connectProducts() throws SQLException {
        logger.info("Connecting to the product database ...");
        try {
            Connection conn = DriverManager.getConnection(URL_PRODUCTS);

            createProductTables(conn);

            return conn;
        } catch (SQLException e) {
            logger.error("Error connecting to the product database.", e);
            throw e;
        }
    }

    /**
     * Creates the product tables if they do not exist.
     * @param conn the Connection object to the database.
     * @throws SQLException if there is an error creating the tables.
     */
    private static void createProductTables(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);
            stat.execute("""
                CREATE TABLE IF NOT EXISTS subcategories (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      name TEXT NOT NULL,
                      category_id INTEGER NOT NULL,
                      FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
                      UNIQUE (name, category_id)
                  );
            """);
            stat.execute("""
                CREATE TABLE IF NOT EXISTS product_references (
                    ref TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    brand TEXT NOT NULL,
                    description TEXT NOT NULL,
                    subcategory_id INTEGER NOT NULL,
                    FOREIGN KEY (subcategory_id) REFERENCES subcategories(id) ON DELETE RESTRICT
                );
            """);
            stat.execute("""
                CREATE TABLE IF NOT EXISTS products (
                      id TEXT PRIMARY KEY,
                      product_ref TEXT NOT NULL,
                      cost REAL NOT NULL,
                      price REAL NOT NULL,
                      stock INTEGER NOT NULL,
                      bill TEXT NOT NULL,
                      date DATE NOT NULL,
                      FOREIGN KEY (product_ref) REFERENCES product_references(ref) ON DELETE CASCADE -- Si se borra la referencia, se borran sus entradas de stock? O RESTRICT? A definir.
                  );
            """);
            stat.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id TEXT PRIMARY KEY,
                    product_ref TEXT NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    comment TEXT,
                    customer_name TEXT,
                    date DATE NOT NULL,
                    FOREIGN KEY (product_ref) REFERENCES product_references(ref) ON DELETE CASCADE
                );
            """);
            logger.info("Verified/created products tables.");
        } catch (SQLException e) {
            logger.error("Error creating/verifying product tables", e);
            throw e;
        }
    }
}