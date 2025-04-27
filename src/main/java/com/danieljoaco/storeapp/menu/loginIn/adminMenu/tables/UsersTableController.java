package com.danieljoaco.storeapp.menu.loginIn.adminMenu.tables;

import com.danieljoaco.storeapp.menu.signUp.UserFormController;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.Users;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.danieljoaco.storeapp.menu.utils.Utils.alert;
import static com.danieljoaco.storeapp.menu.utils.Utils.capitalize;
import static com.danieljoaco.storeapp.users.UserDao.deleteUserToDb;
import static com.danieljoaco.storeapp.users.UserDao.getAllUsers;

/**
 * Controller for the users table
 */
public class UsersTableController extends BaseTableController<Users> {

    private Admin adminLogin;

    /**
     * Sets the admin user for this controller
     * @param admin The admin user
     */
    public void setAdmin(Admin admin) {
        this.adminLogin = admin;
    }

    @Override
    protected void setupColumns() {
        TableColumn<Users, String> colUserId = createColumn("Id", "id", 80);
        TableColumn<Users, String> colUserName = createColumn("Name", "name", 160);
        TableColumn<Users, String> colUserEmail = createColumn("Email", "email", 200);
        TableColumn<Users, String> colUserType = createColumn("User type", "typeUser", 100);
        configureCapitalizeColumn(colUserType);
        TableColumn<Users, String> colUserCreateAt = createColumn("Create at", "formattedDate", 80);

        // Action columns
        TableColumn<Users, String> colUserEdit = createActionColumn("Edit", 40);
        setupEditColumn(colUserEdit);

        TableColumn<Users, String> colUserDelete = createActionColumn("Delete", 50);
        setupDeleteColumn(colUserDelete);

        tableView.getColumns().addAll(
                colUserId, colUserName, colUserEmail, colUserType, colUserCreateAt, colUserEdit, colUserDelete
        );
    }

    /**
     * Creates an action column
     * @param title The title of the column
     * @param width The preferred width of the column
     * @return The created TableColumn
     */
    private TableColumn<Users, String> createActionColumn(String title, double width) {
        TableColumn<Users, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        return column;
    }

    /**
     * Sets up the edit column with a button that opens the edit form
     * @param column The column to set up
     */
    private void setupEditColumn(TableColumn<Users, String> column) {
        column.setCellFactory(param -> new TableCell<>() {
            final Button editButton = new Button("📝");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    editButton.setOnAction(event -> {
                        Users user = getTableView().getItems().get(getIndex());
                        editUserEntry(user);
                    });
                    setGraphic(editButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Sets up the delete column with a button that confirms and deletes a user
     * @param column The column to set up
     */
    private void setupDeleteColumn(TableColumn<Users, String> column) {
        column.setCellFactory(param -> new TableCell<>() {
            final Button deleteButton = new Button("❌");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    deleteButton.setOnAction(event -> {
                        Users user = getTableView().getItems().get(getIndex());
                        deleteUser(user);
                    });
                    setGraphic(deleteButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Opens the edit form for a user
     * @param user The user to edit
     */
    private void editUserEntry(Users user) {
        try {
            openFormWithController(
                    (UserFormController controller, Stage stage) -> {
                        controller.initializeForEditUser(stage, this.adminLogin, user);
                    });

            loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    private <C> void openFormWithController(ControllerInitializer<C> initializer) throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Edit User");
        stage.setResizable(false);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_form.fxml"));
        Parent root = loader.load();

        C controller = loader.getController();
        initializer.initialize(controller, stage);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Shows a confirmation dialog and deletes the user if confirmed
     * @param user The user to delete
     */
    private void deleteUser(Users user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm elimination");
        alert.setHeaderText("Are you sure you want to eliminate this user?");
        alert.setContentText(user.getName() + " (Date: " + user.getEmail() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUserToDb(user.getId());
                dataList.remove(user);
                System.out.println("Deleted user.");
            }
        });
    }

    @Override
    public void loadData() {
        dataList.clear();
        List<Users> users = getAllUsers();
        dataList.addAll(users);

        if (dataList.isEmpty()) {
            System.out.println("There are no users to show.");
        } else {
            System.out.println("Loaded " + dataList.size() + " users.");
        }
    }

    /**
     * Configures a column to capitalize the displayed text
     * @param column The column to configure
     */
    private void configureCapitalizeColumn(TableColumn<Users, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(capitalize(item));
                }
            }
        });
    }
    @FunctionalInterface
    private interface ControllerInitializer<C> {
        void initialize(C controller, Stage stage);
    }
}