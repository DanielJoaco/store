package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.users.Admin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static com.danieljoaco.storeapp.menu.utils.Utils.setupStage;

public class AdminMenu {

    public void showMenu(Admin admin, Stage primaryStage) {
        if (admin.isAdmin()) {
            try {
                String title = "Admin Menu";
                Stage stageAdminMenu = setupStage(title);

                // Load FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-menu.fxml"));
                Parent root = loader.load();

                // Get the controller and configure it
                AdminMenuController controller = loader.getController();
                controller.initialize(admin, primaryStage, stageAdminMenu);

                // Configure the scene
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
                );
                stageAdminMenu.setScene(scene);

                // Store the controller in the stage's user data for later access if needed
                stageAdminMenu.setUserData(controller);

                // Show the dialog and wait
                stageAdminMenu.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading admin menu: " + e.getMessage());
            }
        }
    }

    public void createNewProduct(Stage parentStage) {
        try {
            String title = "New product";
            Stage newProductStage = setupStage(title);

            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new-product.fxml"));
            Parent root = loader.load();

            // Get the controller and configure it
            NewProductController controller = loader.getController();
            controller.initialize(newProductStage);

            // Configure the scene
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
            );
            newProductStage.setScene(scene);

            // Show the dialog and wait
            newProductStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading new product form: " + e.getMessage());
        }
    }
}