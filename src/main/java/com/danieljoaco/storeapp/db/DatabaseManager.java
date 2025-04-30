package com.danieljoaco.storeapp.db;

import java.sql.*;

import com.danieljoaco.storeapp.orders.Order;
import com.danieljoaco.storeapp.orders.Payment;
import com.danieljoaco.storeapp.orders.StreetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String URL_DATABASE = "jdbc:sqlite:store.db";
    private static final String KEY = "MiClaveDeCifrado123";

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
     * Connects to the unified database and applies encryption settings.
     * @return a Connection object to the database.
     * @throws SQLException if there is an error connecting to the database.
     */
    public static Connection connect() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL_DATABASE);

            applyEncryptionPragmas(conn);
            createAllTables(conn);

            return conn;
        } catch (SQLException e) {
            logger.error("Error connecting to the database", e);
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
            stat.execute("PRAGMA foreign_keys = ON;");  // Ensure foreign key constraints are enforced
        } catch (SQLException e) {
            logger.error("Error when applying database settings", e);
            throw e;
        }
    }

    /**
     * Creates all tables in the unified database if they do not exist.
     * @param conn the Connection object to the database.
     * @throws SQLException if there is an error creating the tables.
     */
    private static void createAllTables(Connection conn) throws SQLException {
        createUserTables(conn);
        createProductTables(conn);
        createOrderTables(conn);
    }

    /**
     * Creates the user tables if they do not exist.
     * @param conn the Connection object to the database.
     * @throws SQLException if there is an error creating the tables.
     */
    private static void createUserTables(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute("""
            CREATE TABLE IF NOT EXISTS user_types (
                id INTEGER PRIMARY KEY,
                type_name TEXT UNIQUE NOT NULL
            );
            """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_user TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                type_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                created_at DATE NOT NULL,
                phone_number TEXT,
                balance REAL DEFAULT 0.0,
                FOREIGN KEY (type_id) REFERENCES user_types(id)
            );
            """);

            // Populate user types if not already populated
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

            logger.info("Verified/created user tables.");
        } catch (SQLException e) {
            logger.error("Error creating/verifying user tables", e);
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
                    FOREIGN KEY (product_ref) REFERENCES product_references(ref) ON DELETE CASCADE
                );
            """);

            stat.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id TEXT PRIMARY KEY,
                    product_ref TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    comment TEXT,
                    date DATE NOT NULL,
                    FOREIGN KEY (product_ref) REFERENCES product_references(ref) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            logger.info("Verified/created product tables.");
        } catch (SQLException e) {
            logger.error("Error creating/verifying product tables", e);
            throw e;
        }
    }

    /**
     * Creates the order tables if they do not exist.
     * @param conn the Connection object to the database.
     * @throws SQLException if there is an error creating the tables.
     */
    private static void createOrderTables(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            // Crear las tablas
            stat.execute("""
            CREATE TABLE IF NOT EXISTS franchises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS payment_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                methods_name TEXT NOT NULL UNIQUE,
                franchise_id INTEGER,
                FOREIGN KEY (franchise_id) REFERENCES franchises(id) ON DELETE RESTRICT
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS streets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS shipping_address (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                street_id INTEGER NOT NULL,
                st_number INTEGER NOT NULL,
                st_letter TEXT,
                cross_st_number INTEGER NOT NULL,
                cross_st_letter TEXT,
                house_number INTEGER NOT NULL,
                house_letter TEXT,
                indications TEXT,
                city TEXT NOT NULL,
                postal_code TEXT NOT NULL,
                state TEXT NOT NULL,
                country TEXT NOT NULL,
                FOREIGN KEY (street_id) REFERENCES streets(id) ON DELETE RESTRICT
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id TEXT PRIMARY KEY,
                user_id INTEGER NOT NULL,
                order_date DATE NOT NULL,
                status TEXT NOT NULL,
                payment_id INTEGER NOT NULL,
                subtotal REAL NOT NULL CHECK (subtotal >= 0),
                shipping_cost REAL NOT NULL CHECK (shipping_cost >= 0),
                tax REAL NOT NULL CHECK (tax >= 0),
                discount REAL NOT NULL CHECK (discount >= 0),
                total REAL NOT NULL CHECK (total >= 0),
                shipping_id TEXT NOT NULL,
                tracking_number TEXT,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                FOREIGN KEY (payment_id) REFERENCES payment_data(id) ON DELETE RESTRICT,
                FOREIGN KEY (shipping_id) REFERENCES shipping_address(id) ON DELETE RESTRICT
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id TEXT NOT NULL,
                product_id TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK (quantity > 0),
                unit_price REAL NOT NULL CHECK (unit_price >= 0),
                item_discount REAL DEFAULT 0.0,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS order_statuses (
                id INTEGER PRIMARY KEY,
                name TEXT UNIQUE NOT NULL
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS order_status_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id TEXT NOT NULL,
                status_id INTEGER NOT NULL,
                date DATE NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (status_id) REFERENCES order_statuses(id) ON DELETE RESTRICT
            );
        """);

            stat.execute("""
            CREATE TABLE IF NOT EXISTS status_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id TEXT NOT NULL,
                status_id INTEGER NOT NULL,
                updated_at DATETIME NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (status_id) REFERENCES order_statuses(id) ON DELETE RESTRICT
            );
        """);

            // Insertar valores iniciales
            insertInitialData(conn);

            logger.info("Verified/created order tables and inserted initial data.");
        } catch (SQLException e) {
            logger.error("Error creating/verifying order tables", e);
            throw e;
        }
    }

    private static void insertInitialData(Connection conn) throws SQLException {
        try (Statement stat = conn.createStatement()) {

            ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM streets;");
            int count = rs.getInt(1);
            if (count == 0) {
                for (StreetType streetType : StreetType.values()) {
                    stat.execute(String.format("INSERT INTO streets (name) VALUES ('%s');", streetType.name()));
                }
            }

            rs = stat.executeQuery("SELECT COUNT(*) FROM payment_data;");
            count = rs.getInt(1);
            if (count == 0) {
                for (Payment.PaymentMethod method : Payment.PaymentMethod.values()) {
                    stat.execute(String.format("INSERT INTO payment_data (methods_name) VALUES ('%s');", method.name()));
                }
            }

            rs = stat.executeQuery("SELECT COUNT(*) FROM franchises;");
            count = rs.getInt(1);
            if (count == 0) {
                for (Payment.Franchises franchise : Payment.Franchises.values()) {
                    stat.execute(String.format("INSERT INTO franchises (name) VALUES ('%s');", franchise.name()));
                }
            }

            rs = stat.executeQuery("SELECT COUNT(*) FROM order_statuses;");
            count = rs.getInt(1);
            if (count == 0) {
                for (Order.Status status : Order.Status.values()) {
                    stat.execute(String.format("INSERT INTO order_statuses (name) VALUES ('%s');", status.name()));
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting initial data", e);
            throw e;
        }
    }

}