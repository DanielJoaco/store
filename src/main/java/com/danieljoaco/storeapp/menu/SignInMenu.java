package com.danieljoaco.storeapp.menu;

import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.Users;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

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
        btnCreateAdmin.setOnAction(e -> createAdminAccess());

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
        Admin admin = createAdminAccess();
        System.out.println("Creating a new Support Agent... (Logic not implemented yet)");
        // Se podría implementar la ventana correspondiente aquí
    }

    /**
     * Lógica para acceder al panel de creación de administrador.
     * Llama al método `createAdminAccess()`.
     */
    private Admin createAdminAccess() {
        Stage adminStage = setupStage("Admin Access");
        VBox root = createRootLayout(adminStage);

        GridPane grid = createAdminAccessGrid();
        Label lblMessage = setupMessageLabel();
        Button btnAccess = setupAccessButton(adminStage, lblMessage);

        grid.add(btnAccess, 0, 3, 2, 1);
        grid.add(lblMessage, 0, 4, 2, 1);
        root.getChildren().addAll(grid, new Region(), lblMessage);

        adminStage.setScene(new Scene(root, 400, 300));
        adminStage.show();

        return currentAdmin; // Devuelve el administrador autenticado o null
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

    private VBox createRootLayout(Stage stage) {
        VBox root = new VBox();
        root.setPadding(new Insets(30));
        root.setSpacing(15);
        root.setAlignment(Pos.CENTER);
        root.prefWidthProperty().bind(stage.widthProperty().multiply(0.9));
        root.prefHeightProperty().bind(stage.heightProperty().multiply(0.9));
        return root;
    }

    private GridPane createAdminAccessGrid() {
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

        parameters = new LoginInParameters(txtEmail.getText(), txtPassword.getText());

        grid.add(lblTitle, 0, 0);
        grid.add(lblEmail, 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(lblPassword, 0, 2);
        grid.add(txtPassword, 1, 2);

        return grid;
    }

    private Button setupAccessButton(Stage stage, Label lblMessage) {
        Button btnAccess = new Button("Access");
        btnAccess.setMinWidth(150);

        // Evento del botón de acceso
        btnAccess.setOnAction(e -> {
            String email = parameters.getEmail(); // Capturar valor del campo de texto
            String password = parameters.getPassword(); // Capturar valor del campo de contraseña

            if (email.isEmpty() || password.isEmpty()) {
                // Mostrar mensaje si algún campo está vacío
                lblMessage.setText("Both fields are required.");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }

            try {
                // Intentar autenticar al administrador
                Users admin = Admin.loginAdmin(email, password);

                if (admin.isAdmin()) {
                    // Si es exitoso, mostrar mensaje y cerrar la ventana
                    lblMessage.setText("Access successful.");
                    lblMessage.setStyle("-fx-text-fill: green;");
                    currentAdmin = (Admin) admin;

                    PauseTransition pause = new PauseTransition(Duration.millis(2000));
                    pause.setOnFinished(event -> stage.close());
                    pause.play();
                } else {
                    // Si las credenciales no corresponden a un administrador
                    lblMessage.setText("Invalid email or password.");
                    lblMessage.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) {
                // Mostrar mensaje en caso de error
                lblMessage.setText("Error: " + ex.getMessage());
                lblMessage.setStyle("-fx-text-fill: red;");
                ex.printStackTrace(); // Registrar información para el desarrollador
            }
        });

        return btnAccess;
    }

    private Label setupMessageLabel() {
        Label lblMessage = new Label();
        lblMessage.setWrapText(true);
        lblMessage.setStyle("-fx-max-width: 1000;");
        lblMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        GridPane.setHgrow(lblMessage, Priority.ALWAYS);
        return lblMessage;
    }

    private void handleAdminLogin(Stage stage, String email, String password, Label lblMessage) {
        if (email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Both fields are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Users admin = Admin.loginAdmin(email, password);

            if (admin.isAdmin()) {
                lblMessage.setText("Access successful.");
                lblMessage.setStyle("-fx-text-fill: green;");
                currentAdmin = (Admin) admin;

                PauseTransition pause = new PauseTransition(Duration.millis(2000));
                pause.setOnFinished(event -> stage.close());
                pause.play();
            } else {
                lblMessage.setText("Invalid email or password.");
                lblMessage.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception ex) {
            lblMessage.setText("An error occurred. Please try again.");
            lblMessage.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
    }
}