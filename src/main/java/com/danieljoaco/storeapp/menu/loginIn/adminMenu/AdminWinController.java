package com.danieljoaco.storeapp.menu.loginIn.adminMenu;

import com.danieljoaco.storeapp.menu.loginIn.adminMenu.SearchEngineController.SearchType;
import com.danieljoaco.storeapp.menu.loginIn.adminMenu.tables.OrdersTableController;
import com.danieljoaco.storeapp.menu.loginIn.adminMenu.tables.ProductsTableController;
import com.danieljoaco.storeapp.menu.loginIn.adminMenu.tables.UsersTableController;
import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.Users;
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

import static com.danieljoaco.storeapp.db.ProductsDao.deleteProductToDb;
import static com.danieljoaco.storeapp.menu.signUp.SignUpMenuController.newUser;
import static com.danieljoaco.storeapp.menu.utils.Utils.alert;

public class AdminWinController implements Initializable {

    @FXML
    private MenuItem menuNewProduct, menuNewEntry, menuEditProduct, menuDeleteProduct, newCustomer, newSupportAgent, newAdmin;

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
        menuDeleteProduct.setOnAction(actionEvent -> deleteProductbyRef());

        newCustomer.setOnAction(event -> newUser(Users.UserType.CUSTOMER.name(), null));
        newSupportAgent.setOnAction(event -> newUser(Users.UserType.SUPPORT_AGENT.name(), adminLogin));
        newAdmin.setOnAction(event -> newUser(Users.UserType.ADMIN.name(), adminLogin));
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
        // Order data loading would be implemented here when needed
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
            ProductReference selectedProduct = showSearchDialog(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                openEditProdDataForm(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void deleteProductbyRef() {
        try {
            SearchType searchType = SearchType.PRODUCT;
            ProductReference selectedProduct = showSearchDialog(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                deleteProduct(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void newEntry() {
        try {
            SearchType searchType = SearchType.PRODUCT;
            ProductReference selectedProduct = showSearchDialog(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                openNewEntry(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private ProductReference showSearchDialog(String title, String searchText) throws IOException {
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

    private void openEditProdDataForm(ProductReference productToEdit) {
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

    private void openNewEntry(ProductReference productEntry) {
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

    private void deleteProduct(ProductReference product) {
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

    @FunctionalInterface
    private interface ControllerInitializer<T> {
        void initialize(T controller, Stage stage);
    }
}