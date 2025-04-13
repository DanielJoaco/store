package com.danieljoaco.storeapp.menu;

import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.Customer;
import com.danieljoaco.storeapp.users.SupportAgent;
import com.danieljoaco.storeapp.users.Users;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.SQLException;
import static com.danieljoaco.storeapp.menu.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidEmail;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidPassword;

public class LoginIn {

    private static Users userLogin;

    static Users loginInUsers(String typeUser) {

        String tittle;
        switch (typeUser) {
            case "ADMIN" -> tittle = "Ingreso como Administrador";
            case "SUPPORT_AGENT" -> tittle = "Ingreso como Agente de Soporte";
            case "CUSTOMER" -> tittle = "Ingreso como Cliente";
            default -> tittle = "Ingreso como Usuario";
        }
        Stage loginStage = setupStage(tittle);
        loginStage.initModality(Modality.APPLICATION_MODAL);
        GridPane grid = loginInUserGrid(loginStage, typeUser, tittle);
        VBox root = createVBox(0, 20, Pos.CENTER, grid);
        setScene(loginStage, root, 400, 300, "/styles/manuMainStyles.css");
        try {
            loginStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userLogin;
    }

    static GridPane loginInUserGrid(Stage stage, String typeUser, String title) {
        GridPane grid = createGridPane();
        addTitleToGrid(grid, title);

        Label lblEmail = new Label("Email: ");
        TextField txtEmail = new TextField();

        Label lblPassword = new Label("Password: ");
        PasswordField txtPassword = new PasswordField();

        Label lblError = createErrorLabel();

        Button btnAccess = new Button("Access");
        btnAccess.setOnAction(event -> {
            String email = txtEmail.getText();
            String password = txtPassword.getText();

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
                    disableControls(lblEmail, txtEmail, lblPassword, txtPassword, btnAccess);
                    showSuccess(lblError, "Access granted! Welcome, " + userLogin.getName() + ".");
                    closeAfterDelay(stage);
                } else {
                    throw new IllegalArgumentException("Invalid email or password format.");
                }
            }, "Login successful");
        });

        grid.add(lblEmail, 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(lblPassword, 0, 2);
        grid.add(txtPassword, 1, 2);
        grid.add(btnAccess, 0, 3, 2, 1);
        grid.add(lblError, 0, 4, 2, 1);

        GridPane.setHalignment(btnAccess, HPos.CENTER);
        GridPane.setHalignment(lblError, HPos.CENTER);

        return grid;
    }

}
