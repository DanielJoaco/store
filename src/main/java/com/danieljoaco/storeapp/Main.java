package com.danieljoaco.storeapp;

import com.danieljoaco.storeapp.menu.LoginIn;
import com.danieljoaco.storeapp.products.Category;
import com.danieljoaco.storeapp.products.SubCategory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.danieljoaco.storeapp.menu.SignInMenu;

import java.util.Objects;

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
        LoginIn loginIn = new LoginIn();
        btnLogin.setOnAction(e -> loginIn.show(primaryStage));
        btnExit.setOnAction(e -> {
            System.out.println("Saliendo de la aplicación...");
            primaryStage.close(); // Cerrar la ventana principal
        });

        VBox vbox = new VBox(15); // Espaciado entre botones: 15px
        vbox.setStyle("-fx-alignment: center;"); // Margen y centrado
        vbox.getChildren().addAll(btnRegister, btnLogin, btnExit);


        // Crear una escena y asignarla al escenario principal
        Scene scene = new Scene(vbox, 400, 350);
        String css = Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setScene(scene);

        // Mostrar la ventana
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}