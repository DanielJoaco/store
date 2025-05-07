package com.danieljoaco.storeapp.menu.shippingCar;


import com.danieljoaco.storeapp.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProductViewDev extends Main {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el FXML directamente aquí
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product-view.fxml"));
            Parent root = loader.load();

            ProductViewController controller = loader.getController();
            controller.initialize();

            // Configurar y mostrar la escena
            Scene scene = new Scene(root);
            primaryStage.setTitle("Store Management Product View (DEV)");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting development window: " + e.getMessage());
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}
