package com.danieljoaco.storeapp.menu.loginIn.adminMenu;

import com.danieljoaco.storeapp.Main;
import com.danieljoaco.storeapp.user.Admin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminWinDev extends Main {
    @Override
    public void start(Stage primaryStage) {
        try {
            Admin testAdmin = Admin.loginAdmin(
                    "admin@admin",
                    "admin123"
            );

            // Cargar el FXML directamente aquí
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-win.fxml"));
            Parent root = loader.load();

            // Configurar el controller
            AdminWinController controller = loader.getController();
            controller.loggedInAdmin(testAdmin);

            // Configurar y mostrar la escena
            Scene scene = new Scene(root);
            primaryStage.setTitle("Store Management Admin (DEV)");
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