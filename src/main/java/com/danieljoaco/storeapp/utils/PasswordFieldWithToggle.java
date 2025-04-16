package com.danieljoaco.storeapp.utils;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class PasswordFieldWithToggle extends HBox {

    private final PasswordField passwordField = new PasswordField();
    private final TextField visibleTextField = new TextField();
    private final CheckBox showPassword = new CheckBox("Show");
    private final Label lbl;

    public PasswordFieldWithToggle(String promptText) {
        this.lbl = new Label(promptText);
        this.lbl.setStyle("-fx-font-size: 20px;");

        passwordField.setPromptText(promptText);
        visibleTextField.setPromptText(promptText);

        visibleTextField.managedProperty().bind(showPassword.selectedProperty());
        visibleTextField.visibleProperty().bind(showPassword.selectedProperty());
        visibleTextField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordField.managedProperty().bind(showPassword.selectedProperty().not());
        passwordField.visibleProperty().bind(showPassword.selectedProperty().not());

        StackPane passwordContainer = new StackPane(passwordField, visibleTextField);

        // Centrar verticalmente el contenido del HBox
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(passwordContainer, showPassword);
        setSpacing(5);
    }

    public String getText() {
        return showPassword.isSelected() ? visibleTextField.getText() : passwordField.getText();
    }

    public Label getLabel() {
        return lbl;
    }
}