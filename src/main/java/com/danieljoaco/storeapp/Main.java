package com.danieljoaco.storeapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.danieljoaco.storeapp.menu.SignInMenu;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Store App");

        // Crear botones
        Button btnRegister = new Button("Registrarse");
        Button btnLogin = new Button("Iniciar sesión");
        Button btnExit = new Button("Salir");

        // Agregar lógica a los botones
        SignInMenu signInMenu = new SignInMenu();
        btnRegister.setOnAction(e -> signInMenu.show(primaryStage));
        btnLogin.setOnAction(e -> System.out.println("Abrir formulario de inicio de sesión"));
        btnExit.setOnAction(e -> {
            System.out.println("Saliendo de la aplicación...");
            primaryStage.close(); // Cerrar la ventana principal
        });

        VBox vbox = new VBox(15); // Espaciado entre botones: 15px
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center;"); // Margen y centrado
        vbox.getChildren().addAll(btnRegister, btnLogin, btnExit);


        // Crear una escena y asignarla al escenario principal
        Scene scene = new Scene(vbox, 800, 600); // Tamaño: 300x200
        primaryStage.setScene(scene);

        // Mostrar la ventana
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}