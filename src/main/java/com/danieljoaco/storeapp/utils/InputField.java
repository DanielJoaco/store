package com.danieljoaco.storeapp.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class InputField {

    private final TextField tf;
    private final Label lb;

    public InputField(String text) {
        this.lb = new Label(text);
        this.tf = new TextField();
        this.lb.setStyle("-fx-font-size: 20px;");
    }

    public Label getLabel() {
        return this.lb;
    }
    public TextField getTextField() {
        return this.tf;
    }

    public String getText() {
        return tf.getText();
    }

    public void setDisable(boolean disable) {
        this.lb.setDisable(disable);
        this.tf.setDisable(disable);
    }
}
