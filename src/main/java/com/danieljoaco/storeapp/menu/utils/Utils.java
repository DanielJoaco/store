package com.danieljoaco.storeapp.menu.utils;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import static com.danieljoaco.storeapp.utils.UserValidator.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidId;

public class Utils {
    private static final double DEFAULT_PADDING = 50;
    private static final double DEFAULT_GAP = 15;
    private static final int DEFAULT_PAUSE_MS = 1500;

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
    public static VBox createVBox(double padding, double spacing, Pos align, Node... children) {
        VBox v = new VBox(spacing);
        v.setPadding(new Insets(padding));
        v.setAlignment(align);
        if (children != null && children.length > 0) {
            v.getChildren().addAll(children);
        }
        return v;
    }

    public static Label createTittleLabel(String text, int fontSize) {
        Label l = new Label(text);
        l.setFont(Font.font("Macondo", FontWeight.BOLD, fontSize));
        return l;
    }

    public static Label createErrorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: red;");
        l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);
        return l;
    }

    public static Button createMenuButton(String text, EventHandler<ActionEvent> handler) {
        Button b = new Button(text);
        b.setMinWidth(50);
        b.setOnAction(handler);
        return b;
    }
    public static Button createMenuButton(String text, EventHandler<ActionEvent> handler, int minWidth) {
        Button b = new Button(text);
        b.setMinWidth(minWidth);
        b.setOnAction(handler);
        return b;
    }

    public static void setScene(Stage stage, Parent root, int w, int h, String cssPath) {
        Scene s = new Scene(root, w, h);
        if (cssPath != null) {
            String css = Objects.requireNonNull(Utils.class.getResource(cssPath)).toExternalForm();
            s.getStylesheets().add(css);
        }
        stage.setScene(s);
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

    public static GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(DEFAULT_PADDING));
        grid.setHgap(DEFAULT_GAP);
        grid.setVgap(DEFAULT_GAP);
        grid.setAlignment(Pos.CENTER);
        return grid;
    }

    public static void addTitleToGrid(GridPane grid, String title) {
        Label lblTitle = createTittleLabel(title, 30);
        GridPane.setColumnSpan(lblTitle, 2);
        GridPane.setHalignment(lblTitle, HPos.CENTER);
        grid.add(lblTitle, 0, 0, 2, 1);
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
                    Objects.requireNonNull(Utils.class.getResource("/styles/manuMainStyles.css")).toExternalForm()
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

}
