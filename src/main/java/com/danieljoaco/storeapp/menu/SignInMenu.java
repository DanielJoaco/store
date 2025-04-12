package com.danieljoaco.storeapp.menu;

import static com.danieljoaco.storeapp.utils.UserValidator.*;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.utils.LoginInParameters;
import javafx.animation.PauseTransition;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.stream.Stream;

public class SignInMenu {
    private Admin currentAdmin; // Almacena un administrador autenticado, si corresponde
    private LoginInParameters parameters;

    /**
     * Muestra el menú principal donde el usuario puede elegir entre crear un cliente,
     * un agente de soporte, un administrador o regresar al menú principal.
     */
    public void show(Stage stage) {
        stage.setTitle("Sign In Menu");

        VBox root = new VBox();
        root.setPadding(new Insets(30));
        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);

        // Título del menú
        Label lblTitle = new Label("Sign In Menu");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Botón para crear un cliente
        Button btnCreateCustomer = new Button("Create Customer");
        btnCreateCustomer.setMinWidth(200);
        btnCreateCustomer.setOnAction(e -> createCustomer());

        // Botón para crear un agente de soporte
        Button btnCreateSupportAgent = new Button("Create Support Agent");
        btnCreateSupportAgent.setMinWidth(200);
        btnCreateSupportAgent.setOnAction(e -> createSupportAgent());

        // Botón para crear un administrador
        Button btnCreateAdmin = new Button("Create Admin");
        btnCreateAdmin.setMinWidth(200);
        btnCreateAdmin.setOnAction(e -> accessingAsAdmin());

        // Botón para regresar al menú principal
        Button btnReturnToMainMenu = new Button("Return to Main Menu");
        btnReturnToMainMenu.setMinWidth(200);
        btnReturnToMainMenu.setOnAction(e -> returnToMainMenu(stage));

        // Agregar todos los botones al contenedor
        root.getChildren().addAll(lblTitle, btnCreateCustomer, btnCreateSupportAgent, btnCreateAdmin, btnReturnToMainMenu);

        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }

    /**
     * Lógica para manejar la creación de un cliente.
     * (Implementación pendiente o simulada por ahora).
     */
    private void createCustomer() {
        System.out.println("Creating a new Customer... (Logic not implemented yet)");
        // Se podría implementar la ventana correspondiente aquí
    }

    /**
     * Lógica para manejar la creación de un agente de soporte.
     * (Implementación pendiente o simulada).
     */
    private void createSupportAgent() {
        Admin admin = accessingAsAdmin();
        if(admin.isAdmin()){
            newUser("Agente Soporte");
        }
    }

    /**
     * Lógica para acceder al panel de creación de administrador.
     * Llama al método `accessingAsAdmin()`.
     */
    private Admin accessingAsAdmin() {
        Stage adminStage = setupStage("Admin Access");

        // Crear la interfaz llamando a createAdminAccessGrid
        GridPane adminAccessGrid = createAdminAccessGrid(adminStage);
        VBox root = new VBox(adminAccessGrid);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);

        Scene adminScene = new Scene(root, 400, 300);
        adminStage.setScene(adminScene);
        adminStage.showAndWait();

        // Retornar administrador autenticado (si lo hay)
        return currentAdmin;
    }
    /**
     * Lógica para regresar al menú principal.
     *
     * @param stage El escenario principal que se actualizará.
     */
    private void returnToMainMenu(Stage stage) {
        // Simulación de regreso al menú principal
        System.out.println("Returning to the main menu...");
        stage.close(); // Cierra esta ventana; se podría invocar `menu.show(primaryStage)` si hay una instancia del menú principal
    }

    // Métodos auxiliares reutilizables para configurar ventanas y diseñar la interfaz
    private Stage setupStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        return stage;
    }

    private GridPane createAdminAccessGrid(Stage adminStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(50));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("Admin Access");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        GridPane.setColumnSpan(lblTitle, 2);
        GridPane.setHalignment(lblTitle, HPos.CENTER);

        Label lblEmail = new Label("Email: ");
        TextField txtEmail = new TextField();

        Label lblPassword = new Label("Password: ");
        PasswordField txtPassword = new PasswordField();

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red;");

        Button btnAccess = new Button("Access");
        btnAccess.setOnAction(event -> {
            String email = txtEmail.getText();
            String password = txtPassword.getText();

            try {
                if (email.isBlank() || password.isBlank()) {
                    throw new IllegalArgumentException("Email and password cannot be blank.");
                } else{
                    if (isValidEmail(email) && isValidPassword(password)) { // Llamada a UserValidator
                        // Si la validación pasa, intentar iniciar sesión como administrador
                        currentAdmin = Admin.loginAdmin(email, password);
                        Stream.of(lblEmail, txtEmail, lblPassword, txtPassword, btnAccess).forEach(node -> node.setDisable(true));
                        lblError.setText("Access granted! Welcome, " + currentAdmin.getName() + ".");
                        lblError.setStyle("-fx-text-fill: green;");

                        // Cerrar la ventana tras un retraso
                        PauseTransition pause = new PauseTransition(Duration.millis(2000));
                        pause.setOnFinished(e -> adminStage.close());
                        pause.play();
                    } else {
                        throw new IllegalArgumentException("Invalid email or password format.");
                    }
                }

            } catch (Exception ex) {
                // Mostrar mensaje de error en caso de excepción
                lblError.setText(ex.getMessage());
                lblError.setStyle("-fx-text-fill: red;");
            }
        });

        // Añadir componentes al grid
        grid.add(lblTitle, 0, 0, 2, 1);
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

    private void newUser(String typeUser) throws Exception {
        Stage newUserStage = setupStage("New" + typeUser);

        // Crear la interfaz llamando a createAdminAccessGrid
        GridPane newUserGrid = createNewUsersGrid(newUserStage, typeUser);
        VBox root = new VBox(newUserGrid);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);

        Scene newUserScene = new Scene(root, 400, 300);
        newUserStage.setScene(newUserScene);
        newUserStage.showAndWait();
    }

    private GridPane createNewUsersGrid(Stage newUserStage, String typeUser) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(50));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        Label lblTitle = new Label("New " + typeUser);
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        GridPane.setColumnSpan(lblTitle, 2);
        GridPane.setHalignment(lblTitle, HPos.CENTER);

        Label lblName = new Label("Name: ");
        TextField txtName = new TextField();
        Label lblEmail = new Label("Email: ");
        TextField txtEmail = new TextField();

        Label lblId = new Label("ID: ");
        TextField txtId = new TextField();

        Label lblPassword = new Label("Password: ");
        PasswordField txtPassword = new PasswordField();
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red;");

        Button btnCreate = new Button("Create");
        btnCreate.setOnAction(event -> {
            String name = txtName.getText();
            String email = txtEmail.getText();
            String id = txtId.getText();
            String password = txtPassword.getText();

        });
        return grid;
    }
}