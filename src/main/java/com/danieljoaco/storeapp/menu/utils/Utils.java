package com.danieljoaco.storeapp.menu.utils;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import static com.danieljoaco.storeapp.utils.UserValidator.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidId;

public class Utils {
    private static final int DEFAULT_PAUSE_MS = 1500;

    public static void validateBasicUserInput(String name, String email, String id) {
        if (Stream.of(name, email, id).anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("All fields must be completed.");
        }
        if (!isValidName(name)) throw new IllegalArgumentException("Invalid name format.");
        if (!isValidEmail(email)) throw new IllegalArgumentException("Invalid email format.");
        if (!isValidId(id)) throw new IllegalArgumentException("Invalid ID format.");
    }

    public static void validateUserInput(String name, String email, String id, String password, String repeatPassword) {
        if (Stream.of(name, email, id, password, repeatPassword).anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("All fields must be completed.");
        }
        if (!isValidName(name)) throw new IllegalArgumentException("Invalid name format.");
        if (!isValidEmail(email)) throw new IllegalArgumentException("Invalid email format.");
        if (!isValidPassword(password)) throw new IllegalArgumentException("Password must meet complexity requirements.");
        if (!password.equals(repeatPassword)) throw new IllegalArgumentException("Passwords do not match.");
        if (!isValidId(id)) throw new IllegalArgumentException("Invalid ID format.");
    }

    public static Stage setupStage(String title) {
        Stage s = new Stage();
        s.setTitle(title);
        return s;
    }

    public static void returnToMainMenu(Stage stageShow, Stage stageClose) {
        System.out.println("Returning to the main menu...");
        stageClose.close();
        stageShow.show();
    }

    public static void tryAction(Label lblError, Runnable action, String successMsg) {
        try {
            action.run();
            showSuccess(lblError, successMsg);
        } catch (Exception ex) {
            showError(lblError, ex.getMessage());
        }
    }

    public static void showError(Label lbl, String message) {
        lbl.setText(message);
        lbl.setStyle("-fx-text-fill: red;");
    }

    public static void showSuccess(Label lbl, String message) {
        lbl.setText(message);
        lbl.setStyle("-fx-text-fill: green;");
    }

    public static void closeAfterDelay(Stage stage) {
        PauseTransition pause = new PauseTransition(Duration.millis(DEFAULT_PAUSE_MS));
        pause.setOnFinished(e -> stage.close());
        pause.play();
    }

    public static void disableControls(Node... controls) {
        Stream.of(controls).forEach(node -> node.setDisable(true));
    }

    public static boolean askConfirmation(String text) {
        try {
            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/fxml/confirmation-dialog.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y establecer el texto
            ConfirmationDialogController controller = loader.getController();
            controller.setText(text);

            // Configurar el escenario
            Stage stage = setupStage("Continue");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(Utils.class.getResource("/styles/styles.css")).toExternalForm()
            );
            stage.setScene(scene);

            // Mostrar el diálogo y esperar
            stage.showAndWait();

            // Retornar el resultado
            return controller.getResult();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String lowerCase = text.toLowerCase();
        lowerCase = lowerCase.replaceAll("_", " ");

        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }

    public static void alert(IOException e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al cargar el formulario de nuevo producto");
        alert.setContentText("No se pudo cargar el formulario. Detalles: " + e.getMessage());
        alert.showAndWait();
        System.err.println("Error loading new product form: " + e.getMessage());
        System.err.println("Error loading new product form: " + e.getMessage());
    }
}
