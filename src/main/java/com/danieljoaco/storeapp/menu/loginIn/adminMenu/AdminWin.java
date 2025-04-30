package com.danieljoaco.storeapp.menu.loginIn.adminMenu;

import com.danieljoaco.storeapp.user.Admin;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminWin extends Application {
    private Admin adminLogin;

    @Override
    public void start(Stage primaryStage) {
        // Este método es requerido por Application, pero no lo usaremos directamente
    }

    public void start(Stage primaryStage, Admin adminLogin) {
        this.adminLogin = adminLogin;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-win.fxml"));
            Parent root = loader.load();

            AdminWinController controller = loader.getController();
            controller.loggedInAdmin(adminLogin);


            Scene scene = new Scene(root);
            primaryStage.setTitle("Store Management Admin");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
