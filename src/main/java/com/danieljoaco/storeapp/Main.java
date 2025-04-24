package com.danieljoaco.storeapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y establecer el escenario principal
            MainController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            // Configurar la escena
            Scene scene = new Scene(root);
            String css = Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm();
            scene.getStylesheets().add(css);

            // Configurar y mostrar el escenario
            primaryStage.setTitle("Store App");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}