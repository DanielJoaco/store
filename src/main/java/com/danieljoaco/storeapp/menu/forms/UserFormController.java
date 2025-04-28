package com.danieljoaco.storeapp.menu.forms;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.users.UserDao.saveUser;
import static com.danieljoaco.storeapp.users.UserDao.updateUserToDb;

import com.danieljoaco.storeapp.users.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Arrays;

public class UserFormController extends FormController {

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

    private Users.UserType userType;
    private ComboBox<String> cbUserType;
    private Label lblUserType;

    @FXML
    public void initialize() {
        this.currentMode = FormMode.CREATE;
        this.setErrorLabel(lblError);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleRepeatPasswordField.textProperty().bindBidirectional(repeatPasswordField.textProperty());
    }

    @FXML
    public void initializeForEditUser(Stage stage, Admin admin, Users user) {
        super.initialize(stage, admin);
        this.currentMode = FormMode.EDIT;
        this.setErrorLabel(lblError);

        txtName.setText(user.getName());
        txtEmail.setText(user.getEmail());
        txtId.setText(user.getId());
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
    public void initializeForEditUser(Stage stage, Admin admin, UserDao.BasicUserInfoDb userInfoDb) {
        super.initialize(stage, admin);
        this.currentMode = FormMode.EDIT;
        this.setErrorLabel(lblError);

        txtName.setText(userInfoDb.name());
        txtEmail.setText(userInfoDb.email());
        txtId.setText(userInfoDb.id());
        lblFormTitle.setText("Edit User");
        lblPassword.setVisible(false);
        passwordField.setVisible(false);
        showPasswordCheckbox.setVisible(false);
        lblRepeatPassword.setVisible(false);
        repeatPasswordField.setVisible(false);
        showRepeatPasswordCheckbox.setVisible(false);

        lblUserType = new Label("User Type:");
        cbUserType = new ComboBox<>();
        String userType = switch (userInfoDb.typeUserId()) {
            case 1 -> "Admin";
            case 2 -> "Support Agent";
            case 3 -> "Customer";
            default -> "Unknown";
        };

        cbUserType.setValue(userType);
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

    public void setup(Stage stage, String title, String typeUser, Admin adminLogin) {
        super.initialize(stage, adminLogin);
        this.userType = Users.UserType.valueOf(typeUser);
        lblFormTitle.setText(title);
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            handleSubmit();
        } catch (Exception e) {
            showFormError(e.getMessage());
        }
    }

    @Override
    protected void handleSubmit() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String id = txtId.getText();

        if (currentMode == FormMode.EDIT) {
            String typeUser = cbUserType.getValue().toUpperCase();
            int typeUserId = switch (typeUser) {
                case "ADMIN" -> 1;
                case "SUPPORT_AGENT" -> 2;
                case "CUSTOMER" -> 3;
                default -> 4;
            };

            try{
                validateBasicUserInput(name, email, id);
                try {
                    BasicUserInfo userInfo = new BasicUserInfo(id, email, name, typeUserId);
                    updateUserToDb(userInfo);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            } catch (Exception e) {
                showFormError(e.getMessage());
                return;
            }

            processAfterSubmit("user", name, "updated");

        } else if (currentMode == FormMode.CREATE) {
            String password = passwordField.getText();
            String repeatPassword = repeatPasswordField.getText();

            tryFormAction(() -> {
                validateUserInput(name, email, id, password, repeatPassword);
                try {
                    createUserByType(userType.name(), id, email, password, name);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }, "User created successfully");

            processAfterSubmit("user", userType.toString(), "created");
        }
    }

    @Override
    protected boolean validateFormData() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String id = txtId.getText();

        try {
            if (currentMode == FormMode.CREATE) {
                String password = passwordField.getText();
                String repeatPassword = repeatPasswordField.getText();
                validateUserInput(name, email, id, password, repeatPassword);
            } else {
                validateBasicUserInput(name, email, id);
            }
            return true;
        } catch (IllegalArgumentException e) {
            showFormError(e.getMessage());
            return false;
        }
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

    @Override
    protected Stage getStage() {
        return (Stage) btnCreate.getScene().getWindow();
    }

    @Override
    protected void resetFields() {
        txtName.clear();
        txtEmail.clear();
        txtId.clear();
        passwordField.clear();
        repeatPasswordField.clear();
        lblError.setText("");
    }

    @Override
    protected void disableAllFields() {
        txtName.setDisable(true);
        txtEmail.setDisable(true);
        txtId.setDisable(true);

        if (passwordField.isVisible()) passwordField.setDisable(true);
        if (visiblePasswordField.isVisible()) visiblePasswordField.setDisable(true);
        if (repeatPasswordField.isVisible()) repeatPasswordField.setDisable(true);
        if (visibleRepeatPasswordField.isVisible()) visibleRepeatPasswordField.setDisable(true);
        if (showPasswordCheckbox.isVisible()) showPasswordCheckbox.setDisable(true);
        if (showRepeatPasswordCheckbox.isVisible()) showRepeatPasswordCheckbox.setDisable(true);

        if (cbUserType != null) cbUserType.setDisable(true);

        btnCreate.setDisable(true);
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