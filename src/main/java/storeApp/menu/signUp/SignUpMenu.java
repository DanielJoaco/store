package storeApp.menu.signUp;

import storeApp.menu.utils.Utils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SignUpMenu {
    public void show(Stage primaryStage) {
        try {
            // Configurar el escenario
            String signUpMenuTitle = "Sign Up Menu";
            Stage signUpStage = Utils.setupStage(signUpMenuTitle);

            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signin-menu.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y configurarlo
            SignUpMenuController controller = loader.getController();
            controller.setStages(primaryStage, signUpStage);

            // Configurar la escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
            );
            signUpStage.setScene(scene);

            // Ocultar el escenario principal y mostrar el de registro
            primaryStage.hide();
            signUpStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}