package com.danieljoaco.storeapp.menu.loginIn;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.*;

import com.danieljoaco.storeapp.menu.adminMenu.*;
import com.danieljoaco.storeapp.users.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginFormController {

    @FXML
    private Label lblFormTitle;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private CheckBox showPasswordCheckbox;

    @FXML
    private Button btnAccess;

    @FXML
    private Label lblError;

    private String userType;
    private Stage loginStage;
    private Stage primaryStage;
    private Stage loginInMenuStage;
    private Users userLogin;

    @FXML
    public void initialize() {
        // Sincroniza los campos de contraseña y texto visible
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    public void setup(String title, String userType, Stage loginStage, Stage primaryStage, Stage loginInMenuStage) {
        this.userType = userType;
        this.loginStage = loginStage;
        this.primaryStage = primaryStage;
        this.loginInMenuStage = loginInMenuStage;
        lblFormTitle.setText(title);
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean show = showPasswordCheckbox.isSelected();
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        visiblePasswordField.setVisible(show);
        visiblePasswordField.setManaged(show);
    }

    @FXML
    private void handleAccess(ActionEvent event) {
        String email = txtEmail.getText();
        String password = getPassword();

        tryAction(lblError, () -> {
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalArgumentException("Email and password cannot be blank.");
            } else if (isValidEmail(email) && isValidPassword(password)) {

                try {
                    switch (userType) {
                        case "ADMIN" -> userLogin = Admin.loginAdmin(email, password);
                        case "SUPPORT_AGENT" -> userLogin = SupportAgent.loginSupportAgent(email, password);
                        case "CUSTOMER" -> userLogin = Customer.loginCustomer(email, password);
                    }
                    showSuccess(lblError, "Success message");

                } catch (SQLException e) {
                    showError(lblError, e.getMessage());
                    return;
                }

                txtEmail.setDisable(true);
                passwordField.setDisable(true);
                visiblePasswordField.setDisable(true);
                showPasswordCheckbox.setDisable(true);

                showSuccess(lblError, "Access granted! Welcome, " + userLogin.getName() + ".");
                closeAfterDelay(loginStage);

                if (primaryStage != null) {
                    loginInMenuStage.close();
                    switch (userType) {
                        case "ADMIN": {
                            AdminMenu adminMenu = new AdminMenu();
                            adminMenu.showMenu((Admin) userLogin, primaryStage);
                            break;
                        }
                        case "SUPPORT_AGENT": {
                            break;
                        }
                        case "CUSTOMER": {
                            break;
                        }
                    }
                }

            } else {
                throw new IllegalArgumentException("Invalid email or password format.");
            }
        }, "Login successful");
    }

    private String getPassword() {
        return showPasswordCheckbox.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
    }

    public Users getUserLogin() {
        return userLogin;
    }
}