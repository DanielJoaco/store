package com.danieljoaco.storeapp;
import java.sql.SQLException;

import static com.danieljoaco.storeapp.menu.Menu.*;

public class Main {

    public static void main(String[] args) {

        try {
            showMenu();
        } catch (SQLException e) {
            System.err.println("Error connecting to the database.");
            throw new RuntimeException(e);
        }
    }

}
