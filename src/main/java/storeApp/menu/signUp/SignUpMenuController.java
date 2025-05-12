package storeApp.menu.signUp;

import static storeApp.menu.loginIn.LoginIn.*;
import static storeApp.user.UserDao.*;
import static storeApp.menu.utils.Utils.*;

import storeApp.menu.forms.UserFormController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import storeApp.user.Admin;
import storeApp.user.User;

import java.io.IOException;
import java.util.Objects;

public class SignUpMenuController {

    @FXML
    private Label lblError;

    private Stage signUpStage;
    private Stage primaryStage;
    private Admin adminLogin;
    private boolean adminExists = false;

    public void initialize() {
        adminExists = adminExists();
    }

    public void setStages(Stage primaryStage, Stage signUpStage) {
        this.primaryStage = primaryStage;
        this.signUpStage = signUpStage;
    }

    @FXML
    private void handleCreateCustomer(ActionEvent event) {
        if (adminExists) {
            tryAction(lblError, this::createCustomer, "");
        } else {
            tryAction(lblError, this::createdAdmin, "");
        }
    }

    @FXML
    private void handleCreateSupportAgent(ActionEvent event) {
        if (adminExists) {
            tryAction(lblError, this::createSupportAgent, "");
        } else {
            tryAction(lblError, this::createdAdmin, "");
        }
    }

    @FXML
    private void handleCreateAdmin(ActionEvent event) {
        createdAdmin();
    }

    @FXML
    private void handleReturnToMainMenu(ActionEvent event) {
        returnToMainMenu(primaryStage, signUpStage);
    }

    private void createCustomer() {
        try {
            newUser(User.UserType.CUSTOMER.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createSupportAgent() {
        adminLogin = null;
        adminLogin = (Admin) loginInUsers(User.UserType.ADMIN.name(), null, null);
        if (adminLogin != null && adminLogin.isAdmin()) {
            newUser(User.UserType.SUPPORT_AGENT.name());
        }
    }

    private void createdAdmin() {
        adminLogin = null;
        if (adminExists) {
            adminLogin = (Admin) loginInUsers(User.UserType.ADMIN.name(), null, null);
            if (adminLogin == null || !adminLogin.isAdmin()) {
                return;
            }
        }

        newUser(adminExists ? "ADMIN" : "FIRST_ADMIN");
    }

    private void newUser(String typeUser) {

        String title = userTittle(typeUser);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_form.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            Stage newUserStage = setupStage(title);
            controller.setup(newUserStage, title, typeUser, this.adminLogin);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
            );
            newUserStage.setScene(scene);
            newUserStage.showAndWait();

        } catch (IOException e) {
            showError(lblError, "Error loading form: " + e.getMessage());
        }
    }

    public static void newUser(String typeUser, Admin adminLogin) {

        String title = userTittle(typeUser);

        try {
            FXMLLoader loader = new FXMLLoader(SignUpMenuController.class.getResource("/fxml/user_form.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();

            Stage newUserStage = setupStage(title);
            controller.setup(newUserStage, title, typeUser, adminLogin);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(SignUpMenuController.class.getResource("/styles/styles.css")).toExternalForm()
            );
            newUserStage.setScene(scene);
            newUserStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String userTittle(String typeUser){
        return switch (typeUser) {
            case "FIRST_ADMIN" -> "Create first admin";
            case "ADMIN" -> "Create new admin";
            case "SUPPORT_AGENT" -> "Create new support agent";
            case "CUSTOMER" -> "Create new customer";
            default -> throw new IllegalStateException("Unexpected value: " + typeUser);
        };
    }
}