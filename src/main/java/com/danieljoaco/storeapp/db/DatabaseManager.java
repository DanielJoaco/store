package com.danieljoaco.storeapp.db;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:encrypted_users.db";
    private static final String KEY = "MiClaveDeCifrado123";  // Clave de cifrado (proteger más adelante)

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // Cargar el driver JDBC de SQLite
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver JDBC de SQLite", e);
        }
    }

    /**
     * Abre (y si es necesario inicializa) la base de datos cifrada.
     * Aplica la clave de SQLCipher, crea la tabla y el trigger si no existen.
     */
    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stat = conn.createStatement()) {
            // Aplicar clave de cifrado
            stat.execute(String.format("PRAGMA key = '%s';", KEY));
            stat.execute("PRAGMA cipher_page_size = 4096;");
            stat.execute("PRAGMA kdf_iter = 64000;");

            // Crear la tabla 'users' si no existe
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
}