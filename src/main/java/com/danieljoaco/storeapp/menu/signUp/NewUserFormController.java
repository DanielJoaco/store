package com.danieljoaco.storeapp.menu.signUp;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.users.UserDao.saveUser;

import com.danieljoaco.storeapp.users.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NewUserFormController {

    @FXML
    private Label lblFormTitle;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtId;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private CheckBox showPasswordCheckbox;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private TextField visibleRepeatPasswordField;

    @FXML
    private CheckBox showRepeatPasswordCheckbox;

    @FXML
    private Button btnCreate;

    @FXML
    private Label lblError;

    private String typeUser;
    private Admin adminLogin;

    @FXML
    public void initialize() {
        // Sincroniza los campos de contraseña y texto visible
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleRepeatPasswordField.textProperty().bindBidirectional(repeatPasswordField.textProperty());
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
    private void toggleRepeatPasswordVisibility() {
        boolean show = showRepeatPasswordCheckbox.isSelected();
        repeatPasswordField.setVisible(!show);
        repeatPasswordField.setManaged(!show);
        visibleRepeatPasswordField.setVisible(show);
        visibleRepeatPasswordField.setManaged(show);
    }

    public void setup(String title, String typeUser, Admin adminLogin) {
        this.typeUser = typeUser;
        this.adminLogin = adminLogin;
        lblFormTitle.setText(title);
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String id = txtId.getText();
        String password = passwordField.getText(); // Actualizado
        String repeatPassword = repeatPasswordField.getText(); // Actualizado

        // El resto del método permanece igual
        tryAction(lblError, () -> {
            validateUserInput(name, email, id, password, repeatPassword);
            try {
                createUserByType(typeUser, id, email, password, name);
            } catch (Exception e) {
                showError(lblError, e.getMessage());
                return;
            }

            // Actualizar esta parte para incluir los nuevos componentes
            disableControls(txtName, txtEmail, txtId,
                    passwordField, visiblePasswordField, showPasswordCheckbox,
                    repeatPasswordField, visibleRepeatPasswordField, showRepeatPasswordCheckbox,
                    btnCreate);

            showSuccess(lblError, typeUser + " created successfully!");
            closeAfterDelay(getStage());
        }, "User created successfully");
    }

    private void createUserByType(String typeUser, String id, String email, String password, String name) throws Exception {
        switch (typeUser) {
            case "FIRST_ADMIN" -> {
                Admin firstAdmin = Admin.createFirtsAdmin(id, email, password, name);
                saveUser(firstAdmin);
            }
            case "ADMIN" -> {
                Admin newAdmin = Admin.createAdmin(id, email, password, name, adminLogin);
                saveUser(newAdmin);
            }
            case "SUPPORT_AGENT" -> {
                SupportAgent newSupport = SupportAgent.createdSupportAgent(id, email, password, name, adminLogin);
                saveUser(newSupport);
            }
            case "CUSTOMER" -> {
                Customer newCustomer = new Customer(id, email, password, name);
                saveUser(newCustomer);
            }
        }
    }

    private Stage getStage() {
        return (Stage) btnCreate.getScene().getWindow();
    }
}