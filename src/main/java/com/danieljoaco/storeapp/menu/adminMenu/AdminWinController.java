package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.users.Users;
import com.danieljoaco.storeapp.menu.adminMenu.SearchEngineController.SearchType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;

import static com.danieljoaco.storeapp.db.ProductsDao.*;
import static com.danieljoaco.storeapp.users.UserDao.*;
import static com.danieljoaco.storeapp.menu.signUp.SignUpMenuController.newUser;
import static com.danieljoaco.storeapp.menu.utils.Utils.*;

public class AdminWinController implements Initializable {

    @FXML
    private MenuItem menuNewProduct, menuNewEntry, menuEditProduct, menuDeleteProduct, newCustomer, newSupportAgent, newAdmin;

    @FXML
    private Button btnProducts, btnUsers, btnOrders;

    @FXML
    private AnchorPane tableContainer;

    // Tabla de productos y sus columnas
    private TableView<Products> tableProducts;

    // Tablas adicionales
    private TableView<Users> tableUsers;
    private TableView<Object> tableOrders;

    // Lista observable para los productos
    private final ObservableList<Products> productsList = FXCollections.observableArrayList();
    private final ObservableList<Users> usersList = FXCollections.observableArrayList();
    private Admin adminLogin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMenu();
        setupButtons();
        setupTables();
    }

    public void loggedInAdmin(Admin adminLogin) {
        this.adminLogin = adminLogin;
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

    private void setupTables() {
        createProductsTable();
        createUsersTable();
        createOrdersTable();

        // Ocultar todas las tablas inicialmente
        tableProducts.setVisible(false);
        tableUsers.setVisible(false);
        tableOrders.setVisible(false);
    }

    public void createNewProduct() {
        try {
            openFormWithController("/fxml/product_form.fxml", "New product",
                    (ProductFormController controller, Stage stage) ->
                            controller.initialize(stage, this.adminLogin));
        } catch (IOException e) {
            alert(e);
        }
    }

    private void createProductsTable() {
        tableProducts = new TableView<>();
        tableProducts.setPrefSize(840, 605);

        TableColumn<Products, String> colProductRef = createColumnProd("Ref", "ref", 50);
        TableColumn<Products, String> colProductName = createColumnProd("Name", "name", 120);
        TableColumn<Products, String> colProductBrand = createColumnProd("Brand", "brand", 60);
        TableColumn<Products, Integer> colProductStock = createColumnProd("Stock", "stock", 45);
        TableColumn<Products, Double> colProductCost = createColumnProd("Cost", "cost", 50);
        TableColumn<Products, Double> colProductPrice = createColumnProd("Price", "price", 50);
        TableColumn<Products, String> colProductBill = createColumnProd("Bill", "bill", 80);

        TableColumn<Products, String> colProductCategory = createColumnProd("Cat.", "category", 70);
        configureCapitalizeProdColumn(colProductCategory);

        TableColumn<Products, String> colProductSubcategory = createColumnProd("Subcat.", "subCategory", 100);
        configureCapitalizeProdColumn(colProductSubcategory);

        TableColumn<Products, String> colProductDate = createColumnProd("Date", "formattedDate", 60);

        // Columnas de acción
        TableColumn<Products, String> colProductEdit = createActionColumn("Edit", 40);
        setupEditColumn(colProductEdit);

        TableColumn<Products, String> colProductDelete = createActionColumn("Delete", 50);
        setupDeleteColumn(colProductDelete);

        tableProducts.getColumns().addAll(
                colProductRef, colProductName, colProductBrand, colProductStock, colProductCost,
                colProductPrice, colProductBill, colProductCategory, colProductSubcategory,
                colProductDate, colProductEdit, colProductDelete
        );

        tableProducts.setItems(productsList);
        tableProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        addTableToContainer(tableProducts);
    }

    private void configureCapitalizeProdColumn(TableColumn<Products, String> column) {
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

    private void configureCapitalizeUsersColumn(TableColumn<Users, String> column) {
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




    private <T> TableColumn<Products, T> createColumnProd(String title, String propertyName, double width) {
        TableColumn<Products, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        return column;
    }

    private <T> TableColumn<Users, T> createColumnUsers(String title, String propertyName, double width) {
        TableColumn<Users, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        return column;
    }

    private TableColumn<Products, String> createActionColumn(String title, double width) {
        TableColumn<Products, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        return column;
    }

    private void setupEditColumn(TableColumn<Products, String> column) {
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
                        Products product = getTableView().getItems().get(getIndex());
                        editProductEntry(product);
                    });
                    setGraphic(editButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupDeleteColumn(TableColumn<Products, String> column) {
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
                        Products product = getTableView().getItems().get(getIndex());
                        deleteProduct(product);
                    });
                    setGraphic(deleteButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void createUsersTable() {
        tableUsers = new TableView<>();
        tableUsers.setPrefSize(840, 605);

        TableColumn<Users, String> colUserId = createColumnUsers("Id", "id", 80);
        TableColumn<Users, String> colUserName = createColumnUsers("Name", "name", 160);
        TableColumn<Users, String> colUserEmail = createColumnUsers("Email", "email", 200);
        TableColumn<Users, String> colUserType = createColumnUsers("User type", "typeUser", 100);
        configureCapitalizeUsersColumn(colUserType);
        TableColumn<Users, String> colUserCreateAt = createColumnUsers("Create at", "formattedDate", 80);

        // Añadir columnas básicas
        tableUsers.getColumns().addAll(colUserId, colUserName, colUserEmail, colUserType, colUserCreateAt);

        tableUsers.setItems(usersList);
        tableProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        addTableToContainer(tableUsers);
    }

    private void createOrdersTable() {
        tableOrders = new TableView<>();
        tableOrders.setPrefSize(840, 605);

        // Añadir columnas básicas
        tableOrders.getColumns().addAll(
                new TableColumn<>("Order ID"),
                new TableColumn<>("Date"),
                new TableColumn<>("Customer")
        );

        addTableToContainer(tableOrders);
        tableOrders.setVisible(false);
    }

    private void addTableToContainer(TableView<?> table) {
        AnchorPane.setTopAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        tableContainer.getChildren().add(table);
    }

    private void showProductsTable() {
        setTableVisibility(true, false, false);
        loadProducts();
        setButtonStyles("#4CAF50", "", "");
    }

    private void showUsersTable() {
        setTableVisibility(false, true, false);
        loadUsers();
        setButtonStyles("", "#2196F3", "");
    }

    private void showOrdersTable() {
        setTableVisibility(false, false, true);
        setButtonStyles("", "", "#FF9800");
    }

    private void setTableVisibility(boolean showProducts, boolean showUsers, boolean showOrders) {
        tableProducts.setVisible(showProducts);
        tableUsers.setVisible(showUsers);
        tableOrders.setVisible(showOrders);
    }

    private void setButtonStyles(String productsStyle, String usersStyle, String ordersStyle) {
        btnProducts.setStyle(productsStyle.isEmpty() ? "" :
                "-fx-background-color: " + productsStyle + "; -fx-text-fill: white;");
        btnUsers.setStyle(usersStyle.isEmpty() ? "" :
                "-fx-background-color: " + usersStyle + "; -fx-text-fill: white;");
        btnOrders.setStyle(ordersStyle.isEmpty() ? "" :
                "-fx-background-color: " + ordersStyle + "; -fx-text-fill: white;");
    }

    private void loadProducts() {
        productsList.clear();
        List<Products> products = getAllProducts();
        productsList.addAll(products);

        if (productsList.isEmpty()) {
            System.out.println("There are no products to show.");
        } else {
            System.out.println("They loaded" + productsList.size() + " products.");
        }
    }

    private void loadUsers() {
        usersList.clear();
        List<Users> users = getAllUsers();
        usersList.addAll(users);

        if (usersList.isEmpty()) {
            System.out.println("There are no users to show.");
        } else {
            System.out.println("They loaded" + usersList.size() + " users.");
        }
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

    private void editProductEntry(Products product) {
        try {
            openFormWithController("/fxml/product_form.fxml", SearchType.PRODUCT.getTittle(),
                    (ProductFormController controller, Stage stage) -> {
                        controller.initializeForEditProduct(stage, this.adminLogin, product);
                    });
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

            loadProducts();  // Recargar la tabla después de la edición
        } catch (IOException e) {
            alert(e);
        }
    }

    private void openNewEntry(ProductReference productEntry) {
        try {
            openFormWithController("/fxml/product_form.fxml", "New entry Product",
                    (ProductFormController controller, Stage stage) ->
                            controller.initializeForNewEntry(stage, this.adminLogin, productEntry));

            loadProducts();  // Recargar la tabla después de la edición
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

    private Scene createStyledScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
        );
        return scene;
    }

    private void deleteProduct(Products product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm elimination");
        alert.setHeaderText("Are you sure you want to eliminate this product entry?");
        alert.setContentText(product.getName() + " (Date: " + product.getDate() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteProductEntryToDb(product.getId());
                productsList.remove(product);
                System.out.println("Deleted product entry.");
            }
        });
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

                productsList.removeIf(p -> p.getRef().equals(refToDelete));

                System.out.println("Deleted all entries of product with reference: " + refToDelete);
            }
        });
    }

    @FunctionalInterface
    private interface ControllerInitializer<T> {
        void initialize(T controller, Stage stage);
    }
}