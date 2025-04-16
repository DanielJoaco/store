package com.danieljoaco.storeapp.menu;

import com.danieljoaco.storeapp.users.*;
import com.danieljoaco.storeapp.utils.*;

import static com.danieljoaco.storeapp.menu.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.*;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.SQLException;

public class LoginIn {

    private static Users userLogin;

    public void show(Stage primaryStage) {

        String loginInMenuTittle = "Login In Menu";
        Stage loginInMenuStage = setupStage(loginInMenuTittle);
        VBox root = createVBox(30, 20, Pos.CENTER);

        Label lblTitle = createTittleLabel(loginInMenuTittle, 24);
        Button btnCreateAdmin = createMenuButton("Login In as Admin", e -> loginInUsers(Users.UserType.ADMIN.name(), primaryStage, loginInMenuStage));
        Button btnCreateSupportAgent = createMenuButton("Login In as Support Agent", e -> loginInUsers(Users.UserType.SUPPORT_AGENT.name(), primaryStage, loginInMenuStage));
        Button btnCreateCustomer = createMenuButton("Login In as Customer", e -> loginInUsers(Users.UserType.CUSTOMER.name(), primaryStage, loginInMenuStage));
        Button btnReturnToMainMenu = createMenuButton("Return to Main Menu", e -> returnToMainMenu(primaryStage, loginInMenuStage));

        root.getChildren().addAll(lblTitle, btnCreateAdmin, btnCreateSupportAgent, btnCreateCustomer, btnReturnToMainMenu);
        setScene(loginInMenuStage, root, 600, 400, "/styles/manuMainStyles.css");
        primaryStage.hide();
        loginInMenuStage.showAndWait();

    }

    static Users loginInUsers(String typeUser, Stage primaryStage, Stage loginInMenuStage) {
        userLogin = null;

        String tittle = "";
        switch (typeUser) {
            case "ADMIN" -> tittle = "Login In Admin";
            case "SUPPORT_AGENT" -> tittle = "Login In Support Agent";
            case "CUSTOMER" -> tittle = "Login In Customer";
        }

        Stage loginStage = setupStage(tittle);
        loginStage.initModality(Modality.APPLICATION_MODAL);
        GridPane grid = loginInUserGrid(loginStage, primaryStage, loginInMenuStage, typeUser, tittle);
        VBox root = createVBox(0, 20, Pos.CENTER, grid);
        setScene(loginStage, root, 500, 300, "/styles/manuMainStyles.css");

        loginStage.showAndWait();
        return userLogin;
    }

    private static GridPane loginInUserGrid(Stage loginStage, Stage primaryStage, Stage loginInMenuStage, String typeUser, String title) {
        GridPane grid = createGridPane();
        addTitleToGrid(grid, title);


        InputField inputEmail = new InputField("Email: ");
        PasswordFieldWithToggle inputPassword = new PasswordFieldWithToggle("Password: ");
        Label lblError = createErrorLabel();

        Button btnAccess = new Button("Access");
        btnAccess.setOnAction(event -> {
            String email = inputEmail.getText();
            String password = inputPassword.getText();

            tryAction(lblError, () -> {
                if (email.isBlank() || password.isBlank()) {
                    throw new IllegalArgumentException("Email and password cannot be blank.");
                } else if (isValidEmail(email) && isValidPassword(password)) {

                    try {
                        switch (typeUser) {
                            case "ADMIN" -> userLogin = Admin.loginAdmin(email, password);
                            case "SUPPORT_AGENT" -> userLogin = SupportAgent.loginSupportAgent(email, password);
                            case "CUSTOMER" -> userLogin = Customer.loginCustomer(email, password);
                        }
                        showSuccess(lblError, "Success message");

                    } catch (SQLException e) {
                        showError(lblError, e.getMessage());
                    }

                    inputEmail.setDisable(true);
                    inputPassword.setDisable(true);

                    showSuccess(lblError, "Access granted! Welcome, " + userLogin.getName() + ".");
                    closeAfterDelay(loginStage);

                    if (primaryStage != null){
                        loginInMenuStage.close();
                        switch (typeUser) {
                            case "ADMIN": {
                                AdminMenu adminMenu = new AdminMenu();
                                adminMenu.showMenu((Admin) userLogin, primaryStage);
                            }
                            case "SUPPORT_AGENT": {
                            }
                            case "CUSTOMER": {
                            }
                    }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid email or password format.");
                }
            }, "Login successful");
        });

        grid.add(inputEmail.getLabel(), 0, 1);
        grid.add(inputEmail.getTextField(), 1, 1);
        grid.add(inputPassword.getLabel(), 0, 2);
        grid.add(inputPassword, 1, 2);
        grid.add(btnAccess, 0, 3, 2, 1);
        grid.add(lblError, 0, 4, 2, 1);

        GridPane.setHalignment(btnAccess, HPos.CENTER);
        GridPane.setHalignment(lblError, HPos.CENTER);

        return grid;
    }

}
