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
import static com.danieljoaco.storeapp.menu.utils.Utils.setupStage;

public class AdminWinController implements Initializable {

    @FXML
    private MenuItem menuNewProduct, menuEditProduct, newCustomer, newSupportAgent, newAdmin;

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

        // Inicializar los botones
        setupButtons();

        // Inicializar las tablas
        setupTables();

    }

    public void loggedInAdmin(Admin adminLogin) {
        // Aquí puedes usar el adminLogin como necesites
        // Por ejemplo, guardarlo como una variable de clase
        this.adminLogin = adminLogin;
    }


    private void setupMenu(){
        menuNewProduct.setOnAction(event -> createNewProduct());
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
        // Crear todas las tablas
        createProductsTable();
        createUsersTable();
        createOrdersTable();

        // Asegurar que estén ocultas inicialmente
        tableProducts.setVisible(false);
        tableUsers.setVisible(false);
        tableOrders.setVisible(false);
    }

    public void createNewProduct() {
        try {
            String title = "New product";
            Stage newProductStage = setupStage(title);

            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new-product.fxml"));
            Parent root = loader.load();

            // Get the controller and configure it
            NewProductController controller = loader.getController();
            controller.initialize(newProductStage, this.adminLogin);

            // Configure the scene
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
            );
            newProductStage.setScene(scene);

            // Show the dialog and wait
            newProductStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading new product form: " + e.getMessage());
        }
    }

    private void createProductsTable() {
        // Crear tabla de productos
        tableProducts = new TableView<>();
        tableProducts.setPrefSize(840, 605);

        // Crear columnas para la tabla de productos
        TableColumn<Products, String> colProductRef = new TableColumn<>("Ref");
        TableColumn<Products, String> colProductName = new TableColumn<>("Name");
        TableColumn<Products, Integer> colProductStock = new TableColumn<>("Stock");
        TableColumn<Products, Double> colProductCost = new TableColumn<>("Cost");
        TableColumn<Products, Double> colProductPrice = new TableColumn<>("Price");
        TableColumn<Products, String> colProductBill = new TableColumn<>("Bill");
        TableColumn<Products, String> colProductCategory = new TableColumn<>("Cat.");
        TableColumn<Products, String> colProductSubcategory = new TableColumn<>("Subcat.");
        TableColumn<Products, String> colProductDate = new TableColumn<>("Date");
        TableColumn<Products, String> colProductEdit = new TableColumn<>("Edit");
        TableColumn<Products, String> colProductDelete = new TableColumn<>("Delete");

        // Configurar las columnas
        colProductRef.setCellValueFactory(new PropertyValueFactory<>("ref"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProductStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProductCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colProductPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProductBill.setCellValueFactory(new PropertyValueFactory<>("bill"));
        colProductCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colProductSubcategory.setCellValueFactory(new PropertyValueFactory<>("subCategory"));
        colProductDate.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        // Configurar columnas de acción (Editar y Eliminar)
        setupEditColumn(colProductEdit);
        setupDeleteColumn(colProductDelete);

        // Ajustar ancho de columnas
        colProductRef.setPrefWidth(60);
        colProductName.setPrefWidth(150);
        colProductStock.setPrefWidth(45);
        colProductCost.setPrefWidth(60);
        colProductPrice.setPrefWidth(60);
        colProductBill.setPrefWidth(90);
        colProductCategory.setPrefWidth(70);
        colProductSubcategory.setPrefWidth(100);
        colProductDate.setPrefWidth(60);
        colProductEdit.setPrefWidth(40);
        colProductDelete.setPrefWidth(50);


        // Añadir columnas a la tabla individualmente en lugar de usar addAll
        tableProducts.getColumns().add(colProductRef);
        tableProducts.getColumns().add(colProductName);
        tableProducts.getColumns().add(colProductStock);
        tableProducts.getColumns().add(colProductCost);
        tableProducts.getColumns().add(colProductPrice);
        tableProducts.getColumns().add(colProductBill);
        tableProducts.getColumns().add(colProductCategory);
        tableProducts.getColumns().add(colProductSubcategory);
        tableProducts.getColumns().add(colProductDate);

        tableProducts.getColumns().add(colProductEdit);
        tableProducts.getColumns().add(colProductDelete);

        // Configurar datos (inicialmente vacío)
        tableProducts.setItems(productsList);

        // Permitir scroll vertical
        tableProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Añadir la tabla al contenedor
        AnchorPane.setTopAnchor(tableProducts, 0.0);
        AnchorPane.setRightAnchor(tableProducts, 0.0);
        AnchorPane.setBottomAnchor(tableProducts, 0.0);
        AnchorPane.setLeftAnchor(tableProducts, 0.0);
        tableContainer.getChildren().add(tableProducts);
    }

    private void setupEditColumn(TableColumn<Products, String> column) {
        column.setCellFactory(param -> new TableCell<Products, String>() {
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
        column.setCellFactory(param -> new TableCell<Products, String>() {
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
        // Crear tabla de usuarios (simple para este ejemplo)
        tableUsers = new TableView<>();
        tableUsers.setPrefSize(840, 605);

        // Añadir algunas columnas básicas
        TableColumn<Object, String> colUserId = new TableColumn<>("ID");
        TableColumn<Object, String> colUserName = new TableColumn<>("Name");
        TableColumn<Object, String> colUserEmail = new TableColumn<>("Email");

        // Añadir columnas individualmente en lugar de usar addAll
        tableUsers.getColumns().add(colUserId);
        tableUsers.getColumns().add(colUserName);
        tableUsers.getColumns().add(colUserEmail);

        // Añadir la tabla al contenedor
        AnchorPane.setTopAnchor(tableUsers, 0.0);
        AnchorPane.setRightAnchor(tableUsers, 0.0);
        AnchorPane.setBottomAnchor(tableUsers, 0.0);
        AnchorPane.setLeftAnchor(tableUsers, 0.0);
        tableContainer.getChildren().add(tableUsers);
        tableUsers.setVisible(false);

        // Aquí se cargarían datos reales en una aplicación completa
    }

    private void createOrdersTable() {
        // Crear tabla de órdenes (simple para este ejemplo)
        tableOrders = new TableView<>();
        tableOrders.setPrefSize(840, 605);

        // Añadir algunas columnas básicas
        TableColumn<Object, String> colOrderId = new TableColumn<>("Order ID");
        TableColumn<Object, String> colOrderDate = new TableColumn<>("Date");
        TableColumn<Object, String> colOrderCustomer = new TableColumn<>("Customer");

        // Añadir columnas individualmente en lugar de usar addAll
        tableOrders.getColumns().add(colOrderId);
        tableOrders.getColumns().add(colOrderDate);
        tableOrders.getColumns().add(colOrderCustomer);

        // Añadir la tabla al contenedor
        AnchorPane.setTopAnchor(tableOrders, 0.0);
        AnchorPane.setRightAnchor(tableOrders, 0.0);
        AnchorPane.setBottomAnchor(tableOrders, 0.0);
        AnchorPane.setLeftAnchor(tableOrders, 0.0);
        tableContainer.getChildren().add(tableOrders);
        tableOrders.setVisible(false);

        // Aquí se cargarían datos reales en una aplicación completa
    }

    private void showProductsTable() {
        tableProducts.setVisible(true);
        tableUsers.setVisible(false);
        tableOrders.setVisible(false);

        // Cargar los productos desde la base de datos
        loadProducts();

        // Hacer que el botón de productos se vea seleccionado
        btnProducts.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnUsers.setStyle("");
        btnOrders.setStyle("");
    }

    private void showUsersTable() {
        tableProducts.setVisible(false);
        tableUsers.setVisible(true);
        tableOrders.setVisible(false);

        // Aquí se cargarían los usuarios desde la base de datos

        // Hacer que el botón de usuarios se vea seleccionado
        btnProducts.setStyle("");
        btnUsers.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnOrders.setStyle("");
    }

    private void showOrdersTable() {
        tableProducts.setVisible(false);
        tableUsers.setVisible(false);
        tableOrders.setVisible(true);

        // Aquí se cargarían las órdenes desde la base de datos

        // Hacer que el botón de órdenes se vea seleccionado
        btnProducts.setStyle("");
        btnUsers.setStyle("");
        btnOrders.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
    }

    private void loadProducts() {
        // Limpiar la lista actual
        productsList.clear();

        // Obtener todos los productos de la base de datos
        List<Products> products = getAllProducts();

        // Añadir los productos a la lista observable
        productsList.addAll(products);

        // Si la lista está vacía, mostrar un mensaje
        if (productsList.isEmpty()) {
            System.out.println("No hay productos para mostrar.");
        } else {
            System.out.println("Se cargaron " + productsList.size() + " productos.");
        }
    }

    private void editProductByRef() {
        try {
            String title = SearchType.PRODUCT.getTittle();
            String searchText = SearchType.PRODUCT.getSearchText();
            Stage searchStage = setupStage(title);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search-engine.fxml"));
            Parent root = loader.load();

            SearchEngineController searchEngine = loader.getController();
            searchEngine.initialize(searchStage, title, searchText);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
            );
            searchStage.setScene(scene);

            // Mostrar el diálogo y esperar
            searchStage.showAndWait();

            // Verificar si se seleccionó un producto
            ProductReference selectedProduct = searchEngine.getSelectedProduct();
            if (selectedProduct != null) {
                // Abrir formulario de edición con el producto seleccionado
                openEditForm(selectedProduct);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el formulario de búsqueda: " + e.getMessage());
        }
    }

    private void editProductEntry(Products product) {
        try{
            String title = SearchType.PRODUCT.getTittle();
            Stage searchStage = setupStage(title);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new-product.fxml"));
            Parent root = loader.load();

            NewProductController newProductController = loader.getController();
            newProductController.initialize(searchStage, this.adminLogin);
            newProductController.editProduct(product);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
            );
            searchStage.setScene(scene);

            // Mostrar el diálogo y esperar
            searchStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el formulario de búsqueda: " + e.getMessage());
        }

    }

    private void openEditForm(ProductReference productToEdit) {
        try {
            Stage editStage = setupStage("Edit Product");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-product.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.initialize(editStage, productToEdit, this.adminLogin);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/styles/manuMainStyles.css")).toExternalForm()
            );
            editStage.setScene(scene);
            editStage.showAndWait();

            // Recargar la tabla después de la edición
            loadProducts();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el formulario de edición: " + e.getMessage());
        }
    }


    private void deleteProduct(Products product) {
        // Mostrar un diálogo de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro que deseas eliminar este producto?");
        alert.setContentText(product.getName() + " (Ref: " + product.getRef() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Eliminar el producto de la base de datos
                deleteProductToDb(product.getRef());

                // Eliminar el producto de la tabla
                productsList.remove(product);

                System.out.println("Producto eliminado: " + product.getName());
            }
        });
    }
}