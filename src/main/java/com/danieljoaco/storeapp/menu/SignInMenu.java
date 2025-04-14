package com.danieljoaco.storeapp.menu;

import static com.danieljoaco.storeapp.menu.LoginIn.*;
import static com.danieljoaco.storeapp.users.UserDao.*;
import static com.danieljoaco.storeapp.menu.Utils.*;
import com.danieljoaco.storeapp.users.*;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        Label lblName = new Label("Name: ");
        TextField txtName = new TextField();

        Label lblEmail = new Label("Email: ");
        TextField txtEmail = new TextField();

        Label lblId = new Label("ID: ");
        TextField txtId = new TextField();

        Label lblPassword = new Label("Password: ");
        PasswordField txtPassword = new PasswordField();

        Label lblRepeatPassword = new Label("Repeat Password: ");
        PasswordField txtRepeatPassword = new PasswordField();

        Label lblError = createErrorLabel();

        Button btnCreate = new Button("Create");
        btnCreate.setOnAction(event -> {
            String name = txtName.getText();
            String email = txtEmail.getText();
            String id = txtId.getText();
            String password = txtPassword.getText();
            String repeatPassword = txtRepeatPassword.getText();

            tryAction(lblError, () -> {
                validateUserInput(name, email, id, password, repeatPassword);
                try{
                    createUserByType(typeUser, id, email, password, name);
                } catch (Exception e) {
                    showError(lblError, e.getMessage());
                }

                Node[] controls = {lblName, txtName, lblEmail, txtEmail, lblId, txtId,
                        lblPassword, txtPassword, lblRepeatPassword, txtRepeatPassword, btnCreate};
                disableControls(controls);

                showSuccess(lblError, typeUser + " created successfully!");
                closeAfterDelay(newUserStage);
            }, "User created successfully");
        });

        // Add all controls to grid
        grid.add(lblName, 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(lblEmail, 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(lblId, 0, 3);
        grid.add(txtId, 1, 3);
        grid.add(lblPassword, 0, 4);
        grid.add(txtPassword, 1, 4);
        grid.add(lblRepeatPassword, 0, 5);
        grid.add(txtRepeatPassword, 1, 5);
        grid.add(btnCreate, 0, 6, 2, 1);
        grid.add(lblError, 0, 7, 2, 1);

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