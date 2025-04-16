package com.danieljoaco.storeapp;

import com.danieljoaco.storeapp.menu.loginIn.LoginIn;
import com.danieljoaco.storeapp.menu.signUp.SignUpMenu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnExit;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        SignUpMenu signInMenu = new SignUpMenu();
        signInMenu.show(primaryStage);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        LoginIn loginIn = new LoginIn();
        loginIn.show(primaryStage);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("Saliendo de la aplicación...");
        primaryStage.close();
    }
}