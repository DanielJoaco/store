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
import static com.danieljoaco.storeapp.menu.signUp.SignUpMenuController.newUser;
import static com.danieljoaco.storeapp.menu.utils.Utils.alert;
import static com.danieljoaco.storeapp.menu.utils.Utils.setupStage;

public class AdminWinController implements Initializable {

    @FXML
    private MenuItem menuNewProduct, menuNewEntry, menuEditProduct, newCustomer, newSupportAgent, newAdmin;

    @FXML
    private Button btnProducts, btnUsers, btnOrders;

    @FXML
    private AnchorPane tableContainer;

    // Tabla de productos y sus columnas
    private TableView<Products> tableProducts;

    // Tablas adicionales
    private TableView<Object> tableUsers;
    private TableView<Object> tableOrders;

    // Lista observable para los productos
    private final ObservableList<Products> productsList = FXCollections.observableArrayList();
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
            openFormWithController("/fxml/new-product.fxml", "New product",
                    (NewProductController controller, Stage stage) ->
                            controller.initialize(stage, this.adminLogin));
        } catch (IOException e) {
            alert(e);
        }
    }

    private void createProductsTable() {
        tableProducts = new TableView<>();
        tableProducts.setPrefSize(840, 605);

        // Crear y configurar columnas
        TableColumn<Products, String> colProductRef = createColumn("Ref", "ref", 60);
        TableColumn<Products, String> colProductName = createColumn("Name", "name", 150);
        TableColumn<Products, Integer> colProductStock = createColumn("Stock", "stock", 45);
        TableColumn<Products, Double> colProductCost = createColumn("Cost", "cost", 60);
        TableColumn<Products, Double> colProductPrice = createColumn("Price", "price", 60);
        TableColumn<Products, String> colProductBill = createColumn("Bill", "bill", 90);
        TableColumn<Products, String> colProductCategory = createColumn("Cat.", "category", 70);
        TableColumn<Products, String> colProductSubcategory = createColumn("Subcat.", "subCategory", 100);
        TableColumn<Products, String> colProductDate = createColumn("Date", "formattedDate", 60);

        // Columnas de acción
        TableColumn<Products, String> colProductEdit = createActionColumn("Edit", 40);
        setupEditColumn(colProductEdit);

        TableColumn<Products, String> colProductDelete = createActionColumn("Delete", 50);
        setupDeleteColumn(colProductDelete);

        // Añadir columnas a la tabla
        tableProducts.getColumns().addAll(
                colProductRef, colProductName, colProductStock, colProductCost,
                colProductPrice, colProductBill, colProductCategory, colProductSubcategory,
                colProductDate, colProductEdit, colProductDelete
        );

        // Configurar datos y políticas de redimensionamiento
        tableProducts.setItems(productsList);
        tableProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Añadir la tabla al contenedor
        addTableToContainer(tableProducts);
    }

    private <T> TableColumn<Products, T> createColumn(String title, String propertyName, double width) {
        TableColumn<Products, T> column = new TableColumn<>(title);
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

        // Añadir columnas básicas
        tableUsers.getColumns().addAll(
                new TableColumn<>("ID"),
                new TableColumn<>("Name"),
                new TableColumn<>("Email")
        );

        addTableToContainer(tableUsers);
        tableUsers.setVisible(false);
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
            System.out.println("No hay productos para mostrar.");
        } else {
            System.out.println("Se cargaron " + productsList.size() + " productos.");
        }
    }

    private void editProductByRef() {
        try {
            SearchType searchType = SearchType.PRODUCT;
            ProductReference selectedProduct = showSearchDialog(searchType.getTittle(), searchType.getSearchText());

            if (selectedProduct != null) {
                openEditForm(selectedProduct);
            }
        } catch (IOException e) {
            alert(e);
        }
    }

    private void editProductEntry(Products product) {
        try {
            openFormWithController("/fxml/new-product.fxml", SearchType.PRODUCT.getTittle(),
                    (NewProductController controller, Stage stage) -> {
                        controller.initialize(stage, this.adminLogin);
                        controller.editProductEntry(product);
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

    private void openEditForm(ProductReference productToEdit) {
        try {
            openFormWithController("/fxml/edit-product.fxml", "Edit Product",
                    (EditProductController controller, Stage stage) ->
                            controller.initialize(stage, productToEdit, this.adminLogin));

            loadProducts();  // Recargar la tabla después de la edición
        } catch (IOException e) {
            alert(e);
        }
    }

    private void openNewEntry(ProductReference productEntry) {
        try {
            openFormWithController("/fxml/new-product.fxml", "New entry Product",
                    (NewProductController controller, Stage stage) ->
                            controller.initialize(stage, this.adminLogin, productEntry));

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
                Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
        );
        return scene;
    }

    private void deleteProduct(Products product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro que deseas eliminar este producto?");
        alert.setContentText(product.getName() + " (Ref: " + product.getRef() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteProductToDb(product.getRef());
                productsList.remove(product);
                System.out.println("Producto eliminado: " + product.getName());
            }
        });
    }

    // Interfaz funcional para inicializar controladores
    @FunctionalInterface
    private interface ControllerInitializer<T> {
        void initialize(T controller, Stage stage);
    }
}