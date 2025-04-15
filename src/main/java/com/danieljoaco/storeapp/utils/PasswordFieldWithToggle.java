package com.danieljoaco.storeapp.utils;

import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class PasswordFieldWithToggle extends HBox {

    private final PasswordField passwordField = new PasswordField();
    private final TextField visibleTextField = new TextField();
    private final CheckBox showPassword = new CheckBox("Mostrar");

    public PasswordFieldWithToggle() {
        this("");
    }

    public PasswordFieldWithToggle(String promptText) {
        passwordField.setPromptText(promptText);
        visibleTextField.setPromptText(promptText);

        visibleTextField.managedProperty().bind(showPassword.selectedProperty());
        visibleTextField.visibleProperty().bind(showPassword.selectedProperty());
        visibleTextField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordField.managedProperty().bind(showPassword.selectedProperty().not());
        passwordField.visibleProperty().bind(showPassword.selectedProperty().not());

        StackPane passwordContainer = new StackPane(passwordField, visibleTextField); // Convertido a variable local

        getChildren().addAll(passwordContainer, showPassword);
        setSpacing(5); // Espacio entre el campo de contraseña y el checkbox
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public TextField getVisibleTextField() {
        return visibleTextField;
    }

    public String getText() {
        return showPassword.isSelected() ? visibleTextField.getText() : passwordField.getText();
    }

    public CheckBox getShowPasswordCheckBox() {
        return showPassword;
    }

    public void setPromptText(String prompt) {
        passwordField.setPromptText(prompt);
        visibleTextField.setPromptText(prompt);
    }

}