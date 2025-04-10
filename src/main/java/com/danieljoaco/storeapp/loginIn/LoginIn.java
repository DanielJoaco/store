package com.danieljoaco.storeapp.loginIn;

import com.danieljoaco.storeapp.users.Admin;
import java.sql.SQLException;
import com.danieljoaco.storeapp.users.Customer;
import com.danieljoaco.storeapp.users.SupportAgent;
import com.danieljoaco.storeapp.users.Users;
import com.danieljoaco.storeapp.utils.LoginInParameters;

import static com.danieljoaco.storeapp.utils.Utils.validateFieldsLogin;

public class LoginIn {

    public static Users validatedCredentials(String email, String password, String typeUser) throws SQLException {

        while (true){
            switch (typeUser){
                case "CUSTOMER":
                    Customer customer = Customer.loginCustomer(email, password);
                    System.out.println("Welcome " + customer.getName());
                    return customer;
                case "SUPPORT_AGENT":
                    SupportAgent supportAgent = SupportAgent.loginSupportAgent(email, password);
                    System.out.println("Welcome " + supportAgent.getName());
                    return supportAgent;
                case "ADMIN":
                    Admin admin = Admin.loginAdmin(email, password);
                    System.out.println("Welcome " + admin.getName());
                    return admin;
                default:
                    System.out.println("Invalid user type");
            }
        }

    }

    public static Admin loginInAdmin() throws SQLException {
        System.out.println("Only an admin can created this user.");
        LoginInParameters parameters = validateFieldsLogin();
        return Admin.loginAdmin(parameters.getEmail(), parameters.getPassword());


    }

}
