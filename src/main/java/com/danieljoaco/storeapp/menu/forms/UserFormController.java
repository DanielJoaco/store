package com.danieljoaco.storeapp.menu.forms;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.user.UserDao.saveUser;
import static com.danieljoaco.storeapp.user.UserDao.updateUserToDb;

import com.danieljoaco.storeapp.user.*;

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

    private User.UserType userType;
    private Label lblUserType;
    private ComboBox<String> cbUserType;
    private Label lblPhoneNumber;
    private TextField txtPhoneNumber;
    private boolean isFormValid;

    @FXML
    public void initialize() {
        this.currentMode = FormMode.CREATE;
        this.setErrorLabel(lblError);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleRepeatPasswordField.textProperty().bindBidirectional(repeatPasswordField.textProperty());
    }

    @FXML
    public void initializeForEditUser(Stage stage, Admin admin, User user) {
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
        cbUserType.getItems().addAll(Arrays.stream(User.UserType.values())
                .skip(1)
                .map(type -> capitalize(type.name()))
                .toList());
        GridPane gridPane = (GridPane) btnCreate.getParent();
        gridPane.add(lblUserType, 0, 4);
        gridPane.add(cbUserType, 1, 4);

        // Añadir campo de teléfono si el usuario es CUSTOMER
        if (user.getTypeUser().equals(User.UserType.CUSTOMER.name())) {
            addPhoneNumberField(gridPane);
            if (user instanceof Customer) {
                txtPhoneNumber.setText(((Customer) user).getPhoneNumber());
            }
        }

        // Agregar listener para cambios en el tipo de usuario
        cbUserType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustomer = "Customer".equals(newVal);
            updatePhoneNumberVisibility(gridPane, isCustomer);
        });

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
        cbUserType.getItems().addAll(Arrays.stream(User.UserType.values())
                .skip(1)
                .map(type -> capitalize(type.name()))
                .toList());
        GridPane gridPane = (GridPane) btnCreate.getParent();
        gridPane.add(lblUserType, 0, 4);
        gridPane.add(cbUserType, 1, 4);

        // Añadir campo de teléfono si el usuario es CUSTOMER
        boolean isCustomer = userInfoDb.typeUserId() == 3;
        if (isCustomer) {
            addPhoneNumberField(gridPane);
            if (userInfoDb.phoneNumber() != null) {
                txtPhoneNumber.setText(userInfoDb.phoneNumber());
            }
        }

        // Agregar listener para cambios en el tipo de usuario
        cbUserType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustomerType = "Customer".equals(newVal);
            updatePhoneNumberVisibility(gridPane, isCustomerType);
        });

        btnCreate.setText("Update");

        GridPane.setRowIndex(btnCreate, 5);
        GridPane.setRowIndex(lblError, 6);
    }

    private void addPhoneNumberField(GridPane gridPane) {
        if (lblPhoneNumber == null) {
            lblPhoneNumber = new Label("Phone Number:");
            txtPhoneNumber = new TextField();
            txtPhoneNumber.setPromptText("Enter phone number");
            txtPhoneNumber.setId("txtPhoneNumber");

            GridPane.setRowIndex(lblPhoneNumber, 6);
            GridPane.setColumnIndex(lblPhoneNumber, 0);
            GridPane.setRowIndex(txtPhoneNumber, 6);
            GridPane.setColumnIndex(txtPhoneNumber, 1);

            gridPane.getChildren().addAll(lblPhoneNumber, txtPhoneNumber);
            GridPane.setRowIndex(btnCreate, 7);
            GridPane.setRowIndex(lblError, 8);
        }
    }

    private void updatePhoneNumberVisibility(GridPane gridPane, boolean isCustomer) {
        if (isCustomer) {
            if (lblPhoneNumber == null) {
                addPhoneNumberField(gridPane);
            } else {
                lblPhoneNumber.setVisible(true);
                txtPhoneNumber.setVisible(true);
            }
        } else if (lblPhoneNumber != null) {
            lblPhoneNumber.setVisible(false);
            txtPhoneNumber.setVisible(false);
        }
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
        this.userType = User.UserType.valueOf(typeUser);
        if(typeUser.equals(User.UserType.CUSTOMER.name())) {
            GridPane gridPane = (GridPane) btnCreate.getParent();
            addPhoneNumberField(gridPane);
        }
        lblFormTitle.setText(title);
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateFormData()) {
            return;
        }
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
            String typeUser = cbUserType.getValue().toUpperCase().replace(" ", "_");
            int typeUserId = switch (typeUser) {
                case "ADMIN" -> 1;
                case "SUPPORT_AGENT" -> 2;
                case "CUSTOMER" -> 3;
                default -> 4;
            };

            try {
                validateBasicUserInput(name, email, id);

                String phoneNumber = null;
                if (typeUserId == 3 && txtPhoneNumber != null && txtPhoneNumber.isVisible()) {
                    phoneNumber = txtPhoneNumber.getText();
                    isValidPhoneNumber(phoneNumber);
                }

                BasicUserInfo userInfo = new BasicUserInfo(id, email, name, typeUserId, phoneNumber);
                updateUserToDb(userInfo);
                processAfterSubmit("user", name, "updated");

            } catch (Exception e) {
                showFormError(e.getMessage());
            }

        } else if (currentMode == FormMode.CREATE) {
            String password = passwordField.getText();
            String repeatPassword = repeatPasswordField.getText();

            try {
                validateUserInput(name, email, id, password, repeatPassword);

                String phoneNumber = null;
                if (userType == User.UserType.CUSTOMER && txtPhoneNumber != null) {
                    phoneNumber = txtPhoneNumber.getText();
                    isValidPhoneNumber(phoneNumber);
                }

                createUserByType(userType.name(), id, email, password, name, phoneNumber);
                processAfterSubmit("user", userType.toString(), "created");

            } catch (Exception e) {
                showFormError(e.getMessage());
            }
        }
    }

    @Override
    protected boolean validateFormData() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String id = txtId.getText();

        try {
            if (name.isBlank()) throw new IllegalArgumentException("Name is required");
            if (email.isBlank()) throw new IllegalArgumentException("Email is required");
            if (id.isBlank()) throw new IllegalArgumentException("ID is required");

            if (currentMode == FormMode.CREATE) {
                String password = passwordField.getText();
                String repeat = repeatPasswordField.getText();
                if (password.isBlank() || repeat.isBlank())
                    throw new IllegalArgumentException("Password and repeat are required");
                if (!password.equals(repeat))
                    throw new IllegalArgumentException("Passwords do not match");
            }

            // Verificar el número de teléfono solo si es un cliente y el campo está visible
            boolean isCustomerType = (currentMode == FormMode.CREATE && userType == User.UserType.CUSTOMER) ||
                    (currentMode == FormMode.EDIT && "Customer".equalsIgnoreCase(cbUserType.getValue()));

            if (isCustomerType && txtPhoneNumber != null && txtPhoneNumber.isVisible()) {
                String phone = txtPhoneNumber.getText();
                if (phone == null || phone.isBlank())
                    throw new IllegalArgumentException("Phone number is required for customers");
            }

            isFormValid = true;
            return true;
        } catch (IllegalArgumentException e) {
            showFormError(e.getMessage());
            isFormValid = false;
            return false;
        }
    }

    private void createUserByType(String typeUser, String id, String email, String password, String name, String phoneNumber) throws Exception {
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
                Customer newCustomer = new Customer(id, email, password, name, phoneNumber);
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
        if (txtPhoneNumber != null) {
            txtPhoneNumber.clear();
        }
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
        if (txtPhoneNumber != null && txtPhoneNumber.isVisible()) txtPhoneNumber.setDisable(true);

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
    public record BasicUserInfo(String id, String email, String name, int typeUserId, String phoneNumber) {
    }
}