package storeApp.menu.loginIn.adminMenu;

import storeApp.menu.forms.ProductFormController;
import storeApp.menu.loginIn.adminMenu.SearchEngineController.SearchType;
import storeApp.menu.tables.OrdersTableController;
import storeApp.menu.tables.ProductsTableController;
import storeApp.menu.tables.UsersTableController;
import storeApp.menu.forms.UserFormController;
import storeApp.product.ProductInfo;
import storeApp.user.Admin;
import storeApp.user.UserDao;
import storeApp.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static storeApp.db.ProductsDao.deleteProductToDb;
import static storeApp.menu.signUp.SignUpMenuController.newUser;
import static storeApp.menu.utils.Utils.alert;

public class AdminWinController implements Initializable {

    @FXML
    private MenuItem menuNewProduct, menuNewEntry, menuEditProduct, menuDeleteProduct, newCustomer, newSupportAgent, newAdmin, menuEditUser, menuDeleteUser;

    @FXML
    private Button btnProducts, btnUsers, btnOrders;

    @FXML
    private AnchorPane tableContainer;

    // Table controllers
    private ProductsTableController productsTableController;
    private UsersTableController usersTableController;
    private OrdersTableController ordersTableController;

    private Admin adminLogin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableControllers();
        setupMenu();
        setupButtons();

        // Default to showing products table
        showProductsTable();
    }

    public void loggedInAdmin(Admin adminLogin) {
        this.adminLogin = adminLogin;
        productsTableController.setAdmin(adminLogin);
    }

    private void initializeTableControllers() {
        // Initialize table controllers
        productsTableController = new ProductsTableController();
        usersTableController = new UsersTableController();
        ordersTableController = new OrdersTableController();

        // Create tables and add them to the container
        productsTableController.createTable();
        productsTableController.addTableToContainer(tableContainer);

        usersTableController.createTable();
        usersTableController.addTableToContainer(tableContainer);

        ordersTableController.createTable();
        ordersTableController.addTableToContainer(tableContainer);

        // Hide all tables initially
        setTableVisibility(false, false, false);
    }

    private void setupMenu() {
        menuNewProduct.setOnAction(event -> createNewProduct());
        menuNewEntry.setOnAction(event -> newEntry());
        menuEditProduct.setOnAction(event -> editProductByRef());
        menuDeleteProduct.setOnAction(actionEvent -> deleteProductByRef());

        newCustomer.setOnAction(event -> newUser(User.UserType.CUSTOMER.name(), null));
        newSupportAgent.setOnAction(event -> newUser(User.UserType.SUPPORT_AGENT.name(), adminLogin));
        newAdmin.setOnAction(event -> newUser(User.UserType.ADMIN.name(), adminLogin));
        menuEditUser.setOnAction(event -> editUser());
        menuDeleteUser.setOnAction(actionEvent -> deleteUser());

    }

    private void setupButtons() {
        btnProducts.setOnAction(event -> showProductsTable());
        btnUsers.setOnAction(event -> showUsersTable());
        btnOrders.setOnAction(event -> showOrdersTable());
    }

    public void createNewProduct() {
        try {
            openFormWithController("/fxml/product_form.fxml", "New product",
                    (ProductFormController controller, Stage stage) ->
                            controller.initialize(stage, this.adminLogin));

            // Reload products after creating a new one
            productsTableController.loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    private void showProductsTable() {
        setTableVisibility(true, false, false);
        productsTableController.loadData();
        setButtonStyles("#4CAF50", "", "");
    }

    private void showUsersTable() {
        setTableVisibility(false, true, false);
        usersTableController.loadData();
        setButtonStyles("", "#2196F3", "");
    }

    private void showOrdersTable() {
        setTableVisibility(false, false, true);
        ordersTableController.loadData();
        setButtonStyles("", "", "#FF9800");
    }

    private void setTableVisibility(boolean showProducts, boolean showUsers, boolean showOrders) {
        productsTableController.getTableView().setVisible(showProducts);
        usersTableController.getTableView().setVisible(showUsers);
        ordersTableController.getTableView().setVisible(showOrders);
    }

    private void setButtonStyles(String productsStyle, String usersStyle, String ordersStyle) {
        btnProducts.setStyle(productsStyle.isEmpty() ? "" :
                "-fx-background-color: " + productsStyle + "; -fx-text-fill: white;");
        btnUsers.setStyle(usersStyle.isEmpty() ? "" :
                "-fx-background-color: " + usersStyle + "; -fx-text-fill: white;");
        btnOrders.setStyle(ordersStyle.isEmpty() ? "" :
                "-fx-background-color: " + ordersStyle + "; -fx-text-fill: white;");
    }

    private void editProductByRef() {
        try {
            SearchType searchType = SearchType.PRODUCT;
            ProductInfo selectedProduct = showSearchDialogProduct(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                openEditProdDataForm(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void editUser(){
        try {
            SearchType searchType = SearchType.USER;
            UserDao.BasicUserInfoDb selectedUser = showSearchDialogUser(searchType.getTittle(), searchType.getSearchText());

            if (selectedUser != null) {
                openEditUserForm(selectedUser);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void deleteProductByRef() {
        try {
            SearchType searchType = SearchType.PRODUCT;
            ProductInfo selectedProduct = showSearchDialogProduct(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                deleteProduct(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void deleteUser() {
        try {
            SearchType searchType = SearchType.USER;
            UserDao.BasicUserInfoDb selectedUser = showSearchDialogUser(searchType.getTittle(), searchType.getSearchText());

            if (selectedUser != null) {
                deleteUserToDb(selectedUser);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void newEntry() {
        try {
            SearchType searchType = SearchType.PRODUCT;
            ProductInfo selectedProduct = showSearchDialogProduct(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                openNewEntry(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private ProductInfo showSearchDialogProduct(String title, String searchText) throws IOException {
        Stage searchStage = setupStage(title);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search-engine.fxml"));
        Parent root = loader.load();

        SearchEngineController searchEngine = loader.getController();
        searchEngine.initialize(searchStage, title, searchText);

        Scene scene = createStyledScene(root);
        searchStage.setScene(scene);
        searchStage.showAndWait();

        return searchEngine.getSelectedProduct();
    }

    private UserDao.BasicUserInfoDb showSearchDialogUser(String title, String searchText) throws IOException {
        Stage searchStage = setupStage(title);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search-engine.fxml"));
        Parent root = loader.load();

        SearchEngineController searchEngine = loader.getController();
        searchEngine.initialize(searchStage, title, searchText);

        Scene scene = createStyledScene(root);
        searchStage.setScene(scene);
        searchStage.showAndWait();

        return searchEngine.getSelectedUser();
    }

    private void openEditProdDataForm(ProductInfo productToEdit) {
        try {
            openFormWithController("/fxml/product_form.fxml", "Edit Product data",
                    (ProductFormController controller, Stage stage) ->
                            controller.initializeForEditProdData(stage, this.adminLogin, productToEdit));

            // Reload products after editing
            productsTableController.loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    private void openEditUserForm(UserDao.BasicUserInfoDb userInfoDb) {
        try {
            openFormWithController("/fxml/user_form.fxml", "Edit user data",
                    (UserFormController controller, Stage stage) ->
                            controller.initializeForEditUser(stage, this.adminLogin, userInfoDb));

            // Reload users after editing (no products)
            usersTableController.loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    private void openEditUser(UserDao.BasicUserInfoDb userInfoDb) {
        try {
            openFormWithController("/fxml/user_form.fxml", "Edit user data",
                    (UserFormController controller, Stage stage) ->
                            controller.initializeForEditUser(stage, this.adminLogin, userInfoDb));

            // Reload products after editing
            productsTableController.loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    private void openNewEntry(ProductInfo productEntry) {
        try {
            openFormWithController("/fxml/product_form.fxml", "New entry Product",
                    (ProductFormController controller, Stage stage) ->
                            controller.initializeForNewEntry(stage, this.adminLogin, productEntry));

            // Reload products after adding a new entry
            productsTableController.loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    private <T> void openFormWithController(String fxmlPath, String title, ControllerInitializer<T> initializer)
            throws IOException {
        Stage stage = setupStage(title);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        T controller = loader.getController();
        initializer.initialize(controller, stage);

        Scene scene = createStyledScene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private Stage setupStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setResizable(false);
        return stage;
    }

    private Scene createStyledScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
        );
        return scene;
    }

    private void deleteProduct(ProductInfo product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm elimination");
        alert.setHeaderText("Are you sure you want to eliminate this product?");
        alert.setContentText(product.getName() + " (Ref: " + product.getRef() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String refToDelete = product.getRef();
                deleteProductToDb(refToDelete);

                // Reload products after deletion
                productsTableController.loadData();

                System.out.println("Deleted all entries of product with reference: " + refToDelete);
            }
        });
    }

    private void deleteUserToDb(UserDao.BasicUserInfoDb userToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm elimination");
        alert.setHeaderText("Are you sure you want to eliminate this user?");
        alert.setContentText(userToDelete.name() + " (Email: " + userToDelete.email() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String idToDelete = userToDelete.id();
                UserDao.deleteUserToDb(idToDelete);

                // Reload products after deletion
                usersTableController.loadData();

                System.out.printf("Delete user \nId: %s\nName: %s\nEmail: %s\n",
                        userToDelete.id(), userToDelete.name(), userToDelete.email());
            }
        });
    }
    @FunctionalInterface
    private interface ControllerInitializer<T> {
        void initialize(T controller, Stage stage);
    }
}