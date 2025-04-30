package com.danieljoaco.storeapp.menu.loginIn;

import com.danieljoaco.storeapp.menu.utils.Utils;
import com.danieljoaco.storeapp.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoginInController {

    @FXML
    private Label lblTitle;

    @FXML
    private Button btnLoginAdmin;

    @FXML
    private Button btnLoginSupportAgent;

    @FXML
    private Button btnLoginCustomer;

    @FXML
    private Button btnReturnToMainMenu;

    private Stage loginInMenuStage;
    private Stage primaryStage;

    public void setStages(Stage primaryStage, Stage loginInMenuStage) {
        this.primaryStage = primaryStage;
        this.loginInMenuStage = loginInMenuStage;
    }

    @FXML
    private void handleLoginAdmin(ActionEvent event) {
        LoginIn.loginInUsers(User.UserType.ADMIN.name(), primaryStage, loginInMenuStage);
    }

    @FXML
    private void handleLoginSupportAgent(ActionEvent event) {
        LoginIn.loginInUsers(User.UserType.SUPPORT_AGENT.name(), primaryStage, loginInMenuStage);
    }

    @FXML
    private void handleLoginCustomer(ActionEvent event) {
        LoginIn.loginInUsers(User.UserType.CUSTOMER.name(), primaryStage, loginInMenuStage);
    }

    @FXML
    private void handleReturnToMainMenu(ActionEvent event) {
        Utils.returnToMainMenu(primaryStage, loginInMenuStage);
    }
}