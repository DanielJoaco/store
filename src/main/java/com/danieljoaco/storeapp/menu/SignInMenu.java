package com.danieljoaco.storeapp.menu;

import java.sql.SQLException;

import static com.danieljoaco.storeapp.loginIn.LoginIn.loginInAdmin;
import static com.danieljoaco.storeapp.users.UserDao.saveUser;
import static com.danieljoaco.storeapp.utils.Utils.readInt;
import static com.danieljoaco.storeapp.menu.Menu.showMenu;
import static com.danieljoaco.storeapp.utils.Utils.*;

import com.danieljoaco.storeapp.users.SupportAgent;
import com.danieljoaco.storeapp.users.UserDao;
import com.danieljoaco.storeapp.utils.*;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.Customer;

public class SignInMenu {

    static void signInMenu() throws SQLException {

        while(true){
            System.out.print("""
                        \nPlease select an option:
                        1. Register as a Customer
                        2. Register as a Support Agent
                        3. Register an Admin
                        4. Go back to the main menu
                        Enter your option:\s""");

            int option = readInt();

            switch (option){
                case 1: createdCustomer(); break;
                case 2: createdSupportAgent(); break;
                case 3: createdAdmin(); break;
                case 4: showMenu(); return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void createdCustomer() {
        boolean success = false;
        while (!success) {
            try {
                SingUpParameters parameters = validateFields();
                Customer newCustomer = new Customer(parameters.getId(), parameters.getEmail(), parameters.getPassword(), parameters.getName());
                saveUser(newCustomer);
                System.out.println("User created successfully");
                showMenu();
                success = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void createdSupportAgent() {

        boolean success = false;
        while (!success) {
            try {
                LoginInParameters loginInParameters = validateFieldsLogin();
                Admin admin = Admin.loginAdmin(loginInParameters.getEmail(), loginInParameters.getPassword());
                if (admin.isAdmin()) {
                    System.out.println("You are not authorized to create a Support Agent");
                    return;
                }
                SingUpParameters parameters = validateFields();
                SupportAgent supportAgent = SupportAgent.createdSupportAgent(parameters.getId(), parameters.getEmail(), parameters.getPassword(), parameters.getName(), admin);
                saveUser(supportAgent);
                System.out.println("User created successfully");
                success = true;
                showMenu();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void createdAdmin(){

        boolean success = false;
        while (!success) {
            try{
                System.out.print("""
                    Please select an option:
                    1. Register first admin
                    2. Register a new admin
                    3. Go back to the main menu
                    Enter your option:\s""");
                int option = readInt();

                switch (option){
                    case 1 ->{
                        if (UserDao.adminExists()) {
                            throw new IllegalStateException("At least 1 admin already exists, use createdAdmin().");
                        }
                        SingUpParameters parameters = validateFields();
                        Admin firstAdmin = Admin.createFirtsAdmin(parameters.getId(), parameters.getEmail(), parameters.getPassword(), parameters.getName());
                        saveUser(firstAdmin);
                        System.out.println("First admin created successfully");
                        showMenu();
                        success = true;
                    }

                    case 2 ->{
                        Admin admin = loginInAdmin();
                        if(admin.isAdmin()){
                            SingUpParameters parameters = validateFields();
                            Admin newAdmin = Admin.createAdmin(parameters.getId(), parameters.getEmail(), parameters.getPassword(), parameters.getName(), admin);
                            saveUser(newAdmin);
                            System.out.println("User created successfully");
                            showMenu();
                        }
                        success = true;
                    }

                    case 3 -> {
                        showMenu();
                        success = true;
                    }
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

}
