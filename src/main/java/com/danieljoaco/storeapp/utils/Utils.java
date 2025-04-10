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

    public static SingUpParameters validateFields(){
        String name, email, id, password;

        System.out.print("Please enter your name: ");
        name = scanner.nextLine();
        if (!isValidName(name)) throw new IllegalArgumentException("The name can only contain alphabetic characters.");

        System.out.print("Please enter your email: ");
        email = scanner.nextLine();
        if (!isValidEmail(email)) throw new IllegalArgumentException("The email format must be example@example");

        System.out.print("Please enter your identifier: ");
        id = scanner.nextLine();
        if (!isValidId(id)) throw new IllegalArgumentException("The id must only contain numbers and a length between 5 and 15");

        System.out.print("Please enter your password: ");
        password = scanner.nextLine();
        if (!isValidPassword(password)) throw new IllegalArgumentException("The password must be between 8 and 20 characters, at least one letter and one number...");

        return new SingUpParameters(name, email, id, password);
    }

    public static LoginInParameters validateFieldsLogin() throws SQLException {
        String email, password;

        System.out.print("""
                    Enter your credentials:
                    
                    1. Please enter your email:\s""");
        email = scanner.nextLine();
        if (!isValidEmail(email)){
            throw new IllegalArgumentException("The email format must be example@example");
        }

        System.out.print("2. Please enter your password: ");
        password = scanner.nextLine();
        if (!isValidPassword(password)){
            throw new IllegalArgumentException("The password must be between 8 and 20 characters, at least one letter and one number, and can contain alphanumeric characters and the symbols &%$#_.-");
        }

        return new LoginInParameters(email, password);
    }

}
