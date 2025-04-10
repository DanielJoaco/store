package com.danieljoaco.storeapp.menu;

import com.danieljoaco.storeapp.users.Customer;
import com.danieljoaco.storeapp.users.UserDao;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;

public class SignInMenu {

    // Mostrar la ventana principal
    public void show(Stage primaryStage) {
        primaryStage.setTitle("Sign In Menu");

        // Configuración del diseño mediante un GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);

        // Título
        Label title = new Label("Sign In Menu");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        grid.add(title, 0, 0, 2, 1);                   // Agregar el título en la fila 0 (colspan = 2)

        // Botones para seleccionar opciones
        Button btnCustomer = new Button("Registrar Cliente");
        Button btnSupportAgent = new Button("Registrar Agente de Soporte");
        Button btnAdmin = new Button("Registrar Administrador");
        Button btnBack = new Button("Volver al Menú Principal");

        btnCustomer.setMinWidth(200);                  // Anchura mínima de los botones
        btnSupportAgent.setMinWidth(200);
        btnAdmin.setMinWidth(200);
        btnBack.setMinWidth(200);

        // Agregar botones al diseño
        grid.add(btnCustomer, 0, 1);
        grid.add(btnSupportAgent, 0, 2);
        grid.add(btnAdmin, 0, 3);
        grid.add(btnBack, 0, 4);

        // Lógica para cada botón
        btnCustomer.setOnAction(e -> createCustomer());
        btnSupportAgent.setOnAction(e -> createSupportAgent());
        btnAdmin.setOnAction(e -> createAdmin());
        btnBack.setOnAction(e -> returnToMainMenu(primaryStage));

        // Crear y mostrar la escena
        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Lógica para crear un cliente
    private void createCustomer() {
        Stage customerStage = new Stage();
        customerStage.setTitle("Registrar Cliente");

        // GridPane para recoger la información del cliente
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);

        // Campos para el cliente
        Label lblId = new Label("Id: ");
        TextField txtId = new TextField();

        Label lblName = new Label("Nombre: ");
        TextField txtName = new TextField();

        Label lblEmail = new Label("Correo:");
        TextField txtEmail = new TextField();

        Label lblPassword = new Label("Contraseña: ");
        TextField txtPassword = new TextField();

        Button btnSubmit = new Button("Registrar");
        btnSubmit.setMinWidth(150);

        Label lblMessage = new Label();

        // Agregar elementos al GridPane
        grid.add(lblName, 0, 0);
        grid.add(txtPassword, 1, 0);
        grid.add(lblEmail, 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(btnSubmit, 0, 2, 2, 1); // Coloca el botón en el centro
        grid.add(lblMessage, 0, 3, 2, 1);

        // Acción para el botón "Registrar"
        btnSubmit.setOnAction(e -> {
            String id = txtId.getText();
            String name = txtName.getText();
            String email = txtEmail.getText();
            String password = txtPassword.getText();

            if (name.isEmpty() || email.isEmpty()) {
                lblMessage.setText("Todos los campos son obligatorios.");
                lblMessage.setStyle("-fx-text-fill: red;");
            } else {
                Customer customer = new Customer(id, email, password, name); // Crea el cliente
                UserDao.saveUser(customer);                   // Guarda al cliente en la base de datos
                lblMessage.setText("Cliente registrado exitosamente.");
                lblMessage.setStyle("-fx-text-fill: green;");
            }
        });

        // Crear y mostrar la nueva escena
        Scene scene = new Scene(grid, 400, 250);
        customerStage.setScene(scene);
        customerStage.show();
    }

    // Lógica para crear un agente de soporte
    private void createSupportAgent() {
        System.out.println("Registrar Agente de Soporte: Método pendiente de implementación");
        // Implementar lógica similar a createCustomer()
    }

    // Lógica para crear un administrador
    private void createAdmin() {
        System.out.println("Registrar Administrador: Método pendiente de implementación");
        // Implementar lógica similar a createCustomer()
    }

    // Volver al menú principal (simulado en este caso)
    private void returnToMainMenu(Stage primaryStage) {
        System.out.println("Regresando al Menú Principal...");
        primaryStage.close(); // Solo cierra la ventana actual por ahora
    }
}