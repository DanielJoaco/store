package storeApp.menu.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConfirmationDialogController {

    @FXML
    private Label lblConfirmText;

    @FXML
    private Button btnYes;

    @FXML
    private Button btnNo;

    private AtomicBoolean result = new AtomicBoolean(false);

    public void setText(String text) {
        lblConfirmText.setText(text);
    }

    public boolean getResult() {
        return result.get();
    }

    @FXML
    private void handleYes(ActionEvent event) {
        result.set(true);
        closeStage();
    }

    @FXML
    private void handleNo(ActionEvent event) {
        result.set(false);
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) btnYes.getScene().getWindow();
        stage.close();
    }
}