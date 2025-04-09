package com.danieljoaco.storeapp.menu;

import java.sql.SQLException;
import java.util.Scanner;
import static com.danieljoaco.storeapp.utils.UserValidator.*;
import static com.danieljoaco.storeapp.users.UserDao.*;
import com.danieljoaco.storeapp.users.Customer;
import com.danieljoaco.storeapp.users.SupportAgent;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.UserDao;

public class Menu {

    private static int option;
    private static String name;
    private static String email;
    private static String id;
    private static String password;
    private static Admin admin;
    static Scanner scanner = new Scanner(System.in);

    public static void showMenu() throws SQLException {

        System.out.println("""
                Welcome to the Store App!
                Please select an option:
                1. Register a new user
                2. Login
                3. Exit
                Enter your option:\s""");
        option = scanner.nextInt();
        switch (option){
            case 1:
                signInMenu();
                break;
            case 2:
                loginInMenu();
                break;
            case 3:
                System.out.println("See you soon!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option");
                showMenu();
                break;
        }
    }

    private static void signInMenu() throws SQLException {
        System.out.println("""
        Please select an option:
        1. Register as a Customer
        2. Register as a Support Agent
        3. Register an Admin
        4. Go back to the main menu
        Enter your option:\s""");

        try{
            option = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input");
            signInMenu();
        }

        switch (option){
            case 1: createdCustomer(); break;
            case 2: if (loginInAdmin()) createdSupportAgent(); break;
            case 3: createdAdmin(); break;
            case 4: showMenu(); break;
            default:
                System.out.println("Invalid option");
                signInMenu();
        }
    }

    private static void loginInMenu() {

    }

    private static boolean loginInAdmin() throws SQLException {
        System.out.print("""
                Login as an Admin
                Please enter your credentials:\s""");
        validateUserLogin();
        admin = Admin.instanceAdmin(email, password);
        return true;


    }

    private static void createdCustomer(){
        try{
            validateFields();
            Customer newCustomer = new Customer(id, email, password, name);
            saveUser(newCustomer);
            System.out.println("User created successfully");
            showMenu();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            createdCustomer();
            throw new RuntimeException(e);
        }

    }

    private static void createdSupportAgent(){
        try{
            validateFields();
            SupportAgent newSupportAgent = SupportAgent.createdSupportAgent(id, email, password, name, admin);
            saveUser(newSupportAgent);
            System.out.println("User created successfully");
            admin = null;
        } catch (Exception e) {
            createdSupportAgent();
            throw new RuntimeException(e);
        }
    }

    private static void createdAdmin(){

        try{
        System.out.print("""
                Please select an option:
                1. Register first admin
                2. Register a new admin
                3. Go back to the main menu
                Enter your option:\s""");
        option = scanner.nextInt();

        switch (option){
            case 1:
                if (UserDao.adminExists()) {
                    throw new IllegalStateException("At least 1 admin already exists, use createdAdmin().");
                }
                validateFields();
                Admin firstAdmin = Admin.createFirtsAdmin(id, email, password, name);
                saveUser(firstAdmin);
                System.out.println("User created successfully");
                showMenu();
                break;
            case 2:
                if(loginInAdmin()){
                    validateFields();
                    Admin newAdmin = Admin.createAdmin(id, email, password, name, admin);
                    saveUser(newAdmin);
                    System.out.println("User created successfully");
                    admin = null;
                    showMenu();
                }
                break;
            case 3:
                showMenu();
                break;
            default:
                System.out.println("Invalid option");
                createdAdmin();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            createdAdmin();
            throw new RuntimeException(e);
            }
    }

    private static void validateUserLogin(){

        System.out.print("Please enter your email: ");
        email = scanner.next();
        if (!isValidEmail(email)){
            throw new IllegalArgumentException("The email format must be example@example");
        }

        System.out.print("Please enter your password: ");
        password = scanner.next();
        if (!isValidPassword(password)){
            throw new IllegalArgumentException("The password must be between 8 and 20 characters, at least one letter and one number, and can contain alphanumeric characters and the symbols &%$#_.-");
        }

    }

    private static void validateFields(){
        scanner.nextLine();
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
    }
}
