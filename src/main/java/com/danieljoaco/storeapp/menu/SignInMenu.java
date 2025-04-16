package com.danieljoaco.storeapp.menu;

import static com.danieljoaco.storeapp.menu.LoginIn.*;
import static com.danieljoaco.storeapp.users.UserDao.*;
import static com.danieljoaco.storeapp.menu.Utils.*;

import com.danieljoaco.storeapp.users.*;
import com.danieljoaco.storeapp.utils.*;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class SignInMenu {

    private Admin adminLogin;
    private boolean adminExists = false;

    public void show(Stage primaryStage) {

        String signUpMenuTittle = "Sign Up Menu";
        Stage signUpStage = setupStage(signUpMenuTittle);
        VBox root = createVBox(30, 20, Pos.CENTER);

        Label lblTitle = createTittleLabel(signUpMenuTittle, 30);
        Label lblError = createErrorLabel();

        adminExists = adminExists();

        Button btnCreateCustomer = createMenuButton("Create Customer", e -> {
            if (adminExists){
                tryAction(lblError, this::createCustomer, "");
            } else {
                createdAdmin();
            }
        });
        Button btnCreateSupportAgent = createMenuButton("Create Support Agent", e -> {
            if (adminExists) {
                tryAction(lblError, this::createSupportAgent, "");
            } else {
                tryAction(lblError, this::createdAdmin, "");

            }
        });
        Button btnCreateAdmin = createMenuButton("Create Admin", e -> createdAdmin());
        Button btnReturnToMainMenu = createMenuButton("Return to main menu", e -> returnToMainMenu(primaryStage, signUpStage));

        root.getChildren().addAll(lblTitle, btnCreateCustomer, btnCreateSupportAgent, btnCreateAdmin, btnReturnToMainMenu, lblError);
        setScene(signUpStage, root, 600, 400, "/styles/manuMainStyles.css");
        primaryStage.hide();
        signUpStage.showAndWait();
    }

    private void createCustomer() {
        try {
            newUser(Users.UserType.CUSTOMER.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createSupportAgent() {
        adminLogin = null;
        adminLogin = (Admin) loginInUsers(Users.UserType.ADMIN.name(), null, null);
        if (adminLogin != null && adminLogin.isAdmin()) {
            newUser(Users.UserType.SUPPORT_AGENT.name());
        }
    }

    private void createdAdmin() {
        adminLogin = null;
        if (adminExists) {
            adminLogin = (Admin) loginInUsers(Users.UserType.ADMIN.name(), null, null);
            if (adminLogin == null || !adminLogin.isAdmin()) {
                return;
            }
        }

        newUser(adminExists ? "ADMIN" : "FIRST_ADMIN");
    }

    private void newUser(String typeUser){
        String title = switch (typeUser) {
            case "FIRST_ADMIN" -> "Create first admin";
            case "ADMIN" -> "Create new admin";
            case "SUPPORT_AGENT" -> "Create new support agent";
            case "CUSTOMER" -> "Create new customer";
            default -> throw new IllegalStateException("Unexpected value: " + typeUser);
        };

        Stage newUserStage = setupStage(title);
        GridPane newUserGrid = createNewUsersGrid(newUserStage, typeUser, title);
        VBox root = createVBox(0, 20, Pos.CENTER, newUserGrid);
        setScene(newUserStage, root, 550, 450, "/styles/manuMainStyles.css");
        newUserStage.showAndWait();
    }

    private GridPane createNewUsersGrid(Stage newUserStage, String typeUser, String title) {
        GridPane grid = createGridPane();
        addTitleToGrid(grid, title);

        List<InputField> fields = List.of(
                new InputField("Name: "),
                new InputField("Email: "),
                new InputField("ID: ")
        );

        List<PasswordFieldWithToggle> passwordFields = List.of(
                new PasswordFieldWithToggle("Password:"),
                new PasswordFieldWithToggle("Repeat Password:")
        );

        Label lblError = createErrorLabel();
        Button btnCreate = new Button("Create");

        btnCreate.setOnAction(event -> {
            String name = fields.get(0).getText();
            String email = fields.get(1).getText();
            String id = fields.get(2).getText();
            String password = passwordFields.get(0).getText();
            String repeatPassword = passwordFields.get(1).getText();

            tryAction(lblError, () -> {
                validateUserInput(name, email, id, password, repeatPassword);
                try {
                    createUserByType(typeUser, id, email, password, name);
                } catch (Exception e) {
                    showError(lblError, e.getMessage());
                }

                fields.forEach(field -> field.setDisable(true));
                passwordFields.forEach(passwordField -> passwordField.setDisable(true));
                btnCreate.setDisable(true);

                showSuccess(lblError, typeUser + " created successfully!");
                closeAfterDelay(newUserStage);
            }, "User created successfully");
        });

        // Add non-password fields
        for (int i = 0; i < fields.size(); i++) {
            grid.add(fields.get(i).getLabel(), 0, i + 1);
            grid.add(fields.get(i).getTextField(), 1, i + 1);
        }

        // Add password fields using the new component
        int rowIndex = fields.size() + 1;
        for (PasswordFieldWithToggle passwordField : passwordFields) {
            grid.add(passwordField.getLabel(), 0, rowIndex);
            grid.add(passwordField, 1, rowIndex);
            rowIndex++;
        }

        grid.add(btnCreate, 0, rowIndex + 1, 2, 1);
        grid.add(lblError, 0, rowIndex + 2, 2, 1);

        GridPane.setHalignment(btnCreate, HPos.CENTER);
        GridPane.setHalignment(lblError, HPos.CENTER);

        return grid;
    }

    private void createUserByType(String typeUser, String id, String email, String password, String name) throws Exception {
        switch (typeUser) {
            case "FIRST_ADMIN" -> {
                Admin firstAdmin = Admin.createFirtsAdmin(id, email, password, name);
                saveUser(firstAdmin);
            }
            case "ADMIN" -> {
                Admin newAdmin = Admin.createAdmin(id, email, password, name, adminLogin);
                saveUser(newAdmin);
            }
            case "SUPPORT_AGENT" -> {
                SupportAgent newSupport = SupportAgent.createdSupportAgent(id, email, password, name, adminLogin);
                saveUser(newSupport);
            }
            case "CUSTOMER" -> {
                Customer newCustomer = new Customer(id, email, password, name);
                saveUser(newCustomer);
            }
        }
    }

}