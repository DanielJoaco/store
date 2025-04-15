package com.danieljoaco.storeapp.menu;

import static com.danieljoaco.storeapp.menu.LoginIn.*;
import static com.danieljoaco.storeapp.users.UserDao.*;
import static com.danieljoaco.storeapp.menu.Utils.*;

import com.danieljoaco.storeapp.users.*;
import com.danieljoaco.storeapp.utils.InputField;
import com.danieljoaco.storeapp.utils.PasswordFieldWithToggle;

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

        String signUpMenuTittle = "Menu de registro.";
        Stage signUpStage = setupStage(signUpMenuTittle);
        VBox root = createVBox(30, 20, Pos.CENTER);

        Label lblTitle = createTittleLabel(signUpMenuTittle, 24);
        Label lblError = createErrorLabel();

        adminExists = adminExists();

        Button btnCreateCustomer = createMenuButton("Crear Cliente", e -> {
            if (adminExists){
                createCustomer();
            } else {
                createdAdmin();
            }
        });
        Button btnCreateSupportAgent = createMenuButton("Crear Agente Soporte", e -> {
            if (adminExists) {
                createSupportAgent();
            } else {
                createdAdmin();

            }
        });
        Button btnCreateAdmin = createMenuButton("Crear Administrador", e -> createdAdmin());
        Button btnReturnToMainMenu = createMenuButton("Regresar al menu principal", e -> returnToMainMenu(primaryStage, signUpStage));

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
            case "FIRST_ADMIN" -> "Crear Primer Administrador";
            case "ADMIN" -> "Nuevo Administrador";
            case "SUPPORT_AGENT" -> "Nuevo Agente de Soporte";
            case "CUSTOMER" -> "Nuevo Cliente";
            default -> "Nuevo usuario";
        };

        Stage newUserStage = setupStage(title);
        GridPane newUserGrid = createNewUsersGrid(newUserStage, typeUser, title);
        VBox root = createVBox(0, 20, Pos.CENTER, newUserGrid);
        setScene(newUserStage, root, 450, 500, "/styles/manuMainStyles.css");
        newUserStage.showAndWait();
    }

    private GridPane createNewUsersGrid(Stage newUserStage, String typeUser, String title) {
        GridPane grid = createGridPane();
        addTitleToGrid(grid, title);

        InputField inputName = new InputField("Name: ");
        InputField inputEmail = new InputField("Email: ");
        InputField inputId = new InputField("ID: ");
        PasswordFieldWithToggle inputPasswordToggle = new PasswordFieldWithToggle("Password:");
        PasswordFieldWithToggle inputRepeatPasswordToggle = new PasswordFieldWithToggle("Repeat Password:");

        List<InputField> fields = List.of(inputName, inputEmail, inputId);
        Label lblError = createErrorLabel();
        Button btnCreate = new Button("Create");

        btnCreate.setOnAction(event -> {
            String name = inputName.getText();
            String email = inputEmail.getText();
            String id = inputId.getText();
            String password = inputPasswordToggle.getText();
            String repeatPassword = inputRepeatPasswordToggle.getText();

            tryAction(lblError, () -> {
                validateUserInput(name, email, id, password, repeatPassword);
                try {
                    createUserByType(typeUser, id, email, password, name);
                } catch (Exception e) {
                    showError(lblError, e.getMessage());
                }

                fields.forEach(field -> field.setDisable(true));
                inputPasswordToggle.setDisable(true);
                inputRepeatPasswordToggle.setDisable(true);
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
        grid.add(new Label("Password:"), 0, rowIndex);
        grid.add(inputPasswordToggle, 1, rowIndex);
        rowIndex++;
        grid.add(new Label("Repeat Password:"), 0, rowIndex);
        grid.add(inputRepeatPasswordToggle, 1, rowIndex);

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