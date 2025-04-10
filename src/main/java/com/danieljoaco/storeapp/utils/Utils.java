package com.danieljoaco.storeapp.utils;

import java.sql.SQLException;
import java.util.InputMismatchException;
import static com.danieljoaco.storeapp.menu.Menu.*;
import static com.danieljoaco.storeapp.utils.UserValidator.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidPassword;

public class Utils {

    public static int readInt() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("\nInvalid input. Please enter a valid number: ");
                scanner.nextLine();
            }
        }
    }

    public static SingUpParameters validateFields() {
        String name = "", email = "", id = "", password = "";
        scanner.nextLine();

        System.out.println("Enter your  user credentials.");
        while (true) {
            System.out.print("\nPlease enter your name: ");
            name = scanner.nextLine().trim();
            if (isValidName(name)) {
                break;
            }
            System.out.println("Error: The name can only contain alphabetic characters.");
        }

        while (true) {
            System.out.print("\nPlease enter your email: ");
            email = scanner.nextLine().trim();
            if (isValidEmail(email)) {
                break;
            }
            System.out.println("Error: The email format must be example@example.com");
        }

        while (true) {
            System.out.print("\nPlease enter your identifier: ");
            id = scanner.nextLine().trim();
            if (isValidId(id)) {
                break;
            }
            System.out.println("Error: The ID must contain only numbers (5-15 digits).");
        }

        while (true) {
            System.out.print("\nPlease enter your password: ");
            password = scanner.nextLine().trim();
            if (isValidPassword(password)) {
                System.out.print("Repetition of password: ");
                String password2 = scanner.nextLine().trim();
                if (password.equals(password2)) {
                    break;
                } else {
                    System.out.println("Error: Passwords don't match.");
                }
            }
            System.out.println("Error: Password must be 8-20 characters, with at least 1 letter and 1 number.");
        }

        return new SingUpParameters(name, email, id, password);
    }
    public static LoginInParameters validateFieldsLogin() throws SQLException {
        String email, password;
        scanner.nextLine();

        System.out.println("Enter your credentials.");
        while (true) {
            System.out.print("1. Please enter your email:\s");
            email = scanner.nextLine();
            if (isValidEmail(email)) {
                break;
            }
            System.out.println("Error: The email format must be example@example");
        }

        while (true) {
            System.out.print("2. Please enter your password: ");
            password = scanner.nextLine();
            if (isValidPassword(password)) {
                break;
            }
            System.out.println("Error: The password must be between 8 and 20 characters, at least one letter and one number, and can contain alphanumeric characters and the symbols &%$#_.-");
        }

        return new LoginInParameters(email, password);
    }

}
