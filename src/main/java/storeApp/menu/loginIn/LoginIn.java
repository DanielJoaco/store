package storeApp.menu.loginIn;

import storeApp.menu.utils.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import storeApp.user.User;

import java.io.IOException;
import java.util.Objects;

public class LoginIn {

    public void show(Stage primaryStage) {
        try {
            String loginInMenuTittle = "Login In Menu";
            Stage loginInMenuStage = Utils.setupStage(loginInMenuTittle);

            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-menu.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y configurarlo
            LoginInController controller = loader.getController();
            controller.setStages(primaryStage, loginInMenuStage);

            // Configurar la escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
            );
            loginInMenuStage.setScene(scene);

            // Ocultar el escenario principal y mostrar el de login
            primaryStage.hide();
            loginInMenuStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User loginInUsers(String typeUser, Stage primaryStage, Stage loginInMenuStage) {
        User userLogin = null;

        String title = switch (typeUser) {
            case "ADMIN" -> "Login In Admin";
            case "SUPPORT_AGENT" -> "Login In Support Agent";
            case "CUSTOMER" -> "Login In Customer";
            default -> "Login";
        };

        try {
            Stage loginStage = Utils.setupStage(title);
            loginStage.initModality(Modality.APPLICATION_MODAL);

            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(LoginIn.class.getResource("/fxml/login-form.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y configurarlo
            LoginFormController controller = loader.getController();
            controller.setup(title, typeUser, loginStage, primaryStage, loginInMenuStage);

            // Configurar la escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(LoginIn.class.getResource("/styles/styles.css")).toExternalForm()
            );
            loginStage.setScene(scene);

            // Mostrar el diálogo y esperar
            loginStage.showAndWait();

            // Obtener el usuario que inició sesión
            userLogin = controller.getUserLogin();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userLogin;
    }
}