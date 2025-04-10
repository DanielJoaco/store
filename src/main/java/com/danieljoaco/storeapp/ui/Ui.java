package com.danieljoaco.storeapp.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Ui {
    public void start(Stage primaryStage) {
        // Elementos de la interfaz
        Button registerButton = new Button("Register");
        Button loginButton = new Button("Login");
        Button exitButton = new Button("Exit");

        // Eventos para interactuar con tu sistema (ejemplo)
        registerButton.setOnAction(e -> showRegisterWindow());
        loginButton.setOnAction(e -> showLoginWindow());
        exitButton.setOnAction(e -> System.exit(0));

        // Contenedor
        VBox layout = new VBox(10); // Espaciado de 10px
        layout.getChildren().addAll(registerButton, loginButton, exitButton);

        // Escena principal
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Store App");
        primaryStage.show();
    }

    private void showRegisterWindow() {
        // Llamar tu método existente creadoCustomer()
        //SignInMenu.createdCustomer();
    }

    private void showLoginWindow() {
        // Implementar lógica para el Login
        // Ejemplo: LoginIn.loginInAdmin();
    }

}
