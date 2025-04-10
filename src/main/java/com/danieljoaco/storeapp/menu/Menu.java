package com.danieljoaco.storeapp.menu;

import java.sql.SQLException;
import java.util.Scanner;
import static com.danieljoaco.storeapp.utils.Utils.readInt;


public class Menu {

    public static Scanner scanner = new Scanner(System.in);

    public static void showMenu() throws SQLException {



        while (true) {
            System.out.print("""
               \nWelcome to the Store App!
               Please select an option:
               1. Register a new user
               2. Login
               3. Exit
               Enter your option:\s""");

            int option = readInt();

            switch (option) {
                case 1 -> {}//signInMenu();
                case 2 -> { //loginInMenu();
                }
                case 3 -> {
                    System.out.println("See you soon!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

}
