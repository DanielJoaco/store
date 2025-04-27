package com.danieljoaco.storeapp.menu.signUp;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.users.UserDao.saveUser;
import static com.danieljoaco.storeapp.users.UserDao.updateUserToDb;

import com.danieljoaco.storeapp.users.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;

public class UserFormController {

    @FXML
    private TextField txtName, txtId, txtEmail, visiblePasswordField, visibleRepeatPasswordField;

    @FXML
    private PasswordField passwordField, repeatPasswordField;

    @FXML
    private CheckBox showPasswordCheckbox, showRepeatPasswordCheckbox;

    @FXML
    private Button btnCreate;

    @FXML
    private Label lblFormTitle, lblPassword, lblRepeatPassword, lblError;

    private enum FormMode {
        CREATE,
        EDIT
    }

    private FormMode currentMode;
    private Users.UserType userType;
    private Admin adminLogin;
    private ComboBox<String> cbUserType;
    private Label lblUserType;

    @FXML
    public void initialize() {
        this.currentMode = FormMode.CREATE;
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleRepeatPasswordField.textProperty().bindBidirectional(repeatPasswordField.textProperty());
    }

    @FXML
    public void initializeForEditUser(Stage stage, Admin admin, Users user) {
        this.currentMode = FormMode.EDIT;
        this.adminLogin = admin;

        txtName.setText(user.getName());
        txtEmail.setText(user.getEmail());
        txtId.setText(user.getId());
        passwordField.setText(user.getPasswordHash());
        repeatPasswordField.setText(user.getPasswordHash());
        lblFormTitle.setText("Edit User");
        lblPassword.setVisible(false);
        passwordField.setVisible(false);
        showPasswordCheckbox.setVisible(false);
        lblRepeatPassword.setVisible(false);
        repeatPasswordField.setVisible(false);
        showRepeatPasswordCheckbox.setVisible(false);

        lblUserType = new Label("User Type:");
        cbUserType = new ComboBox<>();
        cbUserType.setValue(capitalize(user.getTypeUser()));
        cbUserType.getItems().addAll(Arrays.stream(Users.UserType.values())
                .skip(1)
                .map(type -> capitalize(type.name()))
                .toList());
        GridPane gridPane = (GridPane) btnCreate.getParent();
        gridPane.add(lblUserType, 0, 4);
        gridPane.add(cbUserType, 1, 4);

        btnCreate.setText("Update");

        GridPane.setRowIndex(btnCreate, 5);
        GridPane.setRowIndex(lblError, 6);
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
        this.userType = Users.UserType.valueOf(typeUser);
        this.adminLogin = adminLogin;
        lblFormTitle.setText(title);
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String id = txtId.getText();

        if (currentMode == FormMode.EDIT) {
            String typeUser = cbUserType.getValue().toUpperCase();
            int typeUserId;
            switch (typeUser) {
                case "ADMIN" -> typeUserId = 1;
                case "SUPPORT_AGENT" -> typeUserId = 2;
                case "CUSTOMER" -> typeUserId = 3;
                default -> typeUserId = 4;
            }
            tryAction(lblError, () -> {
                validateBasicUserInput(name, email, id);
                try {
                    BasicUserInfo userInfo = new BasicUserInfo(id, email, name, typeUserId);
                    updateUserToDb(userInfo);
                } catch (Exception e) {
                    showError(lblError, e.getMessage());
                }
            }, "User updated successfully");
        } else if (currentMode == FormMode.CREATE) {
            String password = passwordField.getText();
            String repeatPassword = repeatPasswordField.getText();

            tryAction(lblError, () -> {
                validateUserInput(name, email, id, password, repeatPassword);
                try {
                    createUserByType(userType.name(), id, email, password, name);
                } catch (Exception e) {
                    showError(lblError, e.getMessage());
                }
            }, "User created successfully");

        }

        disableControls(txtName, txtEmail, txtId,
                passwordField, visiblePasswordField, showPasswordCheckbox,
                repeatPasswordField, visibleRepeatPasswordField, showRepeatPasswordCheckbox,
                btnCreate);

        showSuccess(lblError, userType + " created successfully!");
        closeAfterDelay(getStage());
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

    /**
     * Validates the basic user input
     *
     * @param id         The id of the user
     * @param email      The email of the user
     * @param name       The name of the user
     * @param typeUserId The type of user (1: Admin, 2: Support Agent, 3: Customer)
     */
    public record BasicUserInfo(String id, String email, String name, int typeUserId) {
    }
}