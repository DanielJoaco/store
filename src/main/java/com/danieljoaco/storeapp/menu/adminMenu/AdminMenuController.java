package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.users.Admin;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;

public class AdminMenuController {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblError;

    private Admin admin;
    private Stage primaryStage;
    private Stage currentStage;

    public void initialize(Admin admin, Stage primaryStage, Stage currentStage) {
        this.admin = admin;
        this.primaryStage = primaryStage;
        this.currentStage = currentStage;

        // Any additional initialization can go here
    }

    @FXML
    private void handleNewProduct(ActionEvent event) {
        tryAction(lblError, this::createNewProduct, "");
    }

    @FXML
    private void handleSetProduct(ActionEvent event) {
        // Implementation would go here
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        // Implementation would go here
    }

    @FXML
    private void handleSetUser(ActionEvent event) {
        // Implementation would go here
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        // Implementation would go here
    }

    @FXML
    private void handleReturnToMainMenu(ActionEvent event) {
        returnToMainMenu(primaryStage, currentStage);
    }

    private void createNewProduct() {
        AdminMenu adminMenu = new AdminMenu();
        adminMenu.createNewProduct(currentStage);
    }
}