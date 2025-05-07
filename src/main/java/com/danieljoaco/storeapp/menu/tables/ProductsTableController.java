package com.danieljoaco.storeapp.menu.tables;

import com.danieljoaco.storeapp.menu.forms.ProductFormController;
import com.danieljoaco.storeapp.product.Product;
import com.danieljoaco.storeapp.user.Admin;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.danieljoaco.storeapp.db.ProductsDao.deleteProductEntryToDb;
import static com.danieljoaco.storeapp.db.ProductsDao.getAllProducts;
import static com.danieljoaco.storeapp.menu.utils.Utils.alert;
import static com.danieljoaco.storeapp.menu.utils.Utils.capitalize;

/**
 * Controller for the products table
 */
public class ProductsTableController extends BaseTableController<Product> {

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
        TableColumn<Product, String> colProductRef = createColumn("Ref", "ref", 50);
        TableColumn<Product, String> colProductName = createColumn("Name", "name", 120);
        TableColumn<Product, String> colProductBrand = createColumn("Brand", "brand", 60);
        TableColumn<Product, Integer> colProductStock = createColumn("Stock", "stock", 45);
        TableColumn<Product, Double> colProductCost = createColumn("Cost", "cost", 50);
        TableColumn<Product, Double> colProductPrice = createColumn("Price", "price", 50);
        TableColumn<Product, String> colProductBill = createColumn("Bill", "bill", 80);

        TableColumn<Product, String> colProductCategory = createColumn("Cat.", "category", 70);
        configureCapitalizeColumn(colProductCategory);

        TableColumn<Product, String> colProductSubcategory = createColumn("Subcat.", "subCategory", 100);
        configureCapitalizeColumn(colProductSubcategory);

        TableColumn<Product, String> colProductDate = createColumn("Date", "formattedDate", 60);

        // Action columns
        TableColumn<Product, String> colProductEdit = createActionColumn("Edit", 40);
        setupEditColumn(colProductEdit);

        TableColumn<Product, String> colProductDelete = createActionColumn("Delete", 50);
        setupDeleteColumn(colProductDelete);

        tableView.getColumns().addAll(
                colProductRef, colProductName, colProductBrand, colProductStock, colProductCost,
                colProductPrice, colProductBill, colProductCategory, colProductSubcategory,
                colProductDate, colProductEdit, colProductDelete
        );
    }

    @Override
    public void loadData() {
        dataList.clear();
        List<Product> products = getAllProducts();
        dataList.addAll(products);

        if (dataList.isEmpty()) {
            System.out.println("There are no products to show.");
        } else {
            System.out.println("Loaded " + dataList.size() + " products.");
        }
    }

    /**
     * Configures a column to capitalize the displayed text
     * @param column The column to configure
     */
    private void configureCapitalizeColumn(TableColumn<Product, String> column) {
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

    /**
     * Creates an action column
     * @param title The title of the column
     * @param width The preferred width of the column
     * @return The created TableColumn
     */
    private TableColumn<Product, String> createActionColumn(String title, double width) {
        TableColumn<Product, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        return column;
    }

    /**
     * Sets up the edit column with a button that opens the edit form
     * @param column The column to set up
     */
    private void setupEditColumn(TableColumn<Product, String> column) {
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
                        Product product = getTableView().getItems().get(getIndex());
                        editProductEntry(product);
                    });
                    setGraphic(editButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Sets up the delete column with a button that confirms and deletes a product
     * @param column The column to set up
     */
    private void setupDeleteColumn(TableColumn<Product, String> column) {
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
                        Product product = getTableView().getItems().get(getIndex());
                        deleteProduct(product);
                    });
                    setGraphic(deleteButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Opens the edit form for a product
     * @param product The product to edit
     */
    private void editProductEntry(Product product) {
        try {
            openFormWithController(
                    (ProductFormController controller, Stage stage) -> {
                        controller.initializeForEditProduct(stage, this.adminLogin, product);
                    });

            // Reload data after editing
            loadData();
        } catch (IOException e) {
            alert(e);
        }
    }

    /**
     * Shows a confirmation dialog and deletes the product if confirmed
     * @param product The product to delete
     */
    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm elimination");
        alert.setHeaderText("Are you sure you want to eliminate this product entry?");
        alert.setContentText(product.getName() + " (Date: " + product.getDate() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteProductEntryToDb(product.getId());
                dataList.remove(product);
                System.out.println("Deleted product entry.");
            }
        });
    }

    /**
     * Opens a form with the specified controller
     *
     * @param <T>         The type of the controller
     * @param initializer The initializer for the controller
     * @throws IOException If the FXML file cannot be loaded
     */
    private <T> void openFormWithController(ControllerInitializer<T> initializer)
            throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Edit Product entry");
        stage.setResizable(false);

        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/product_form.fxml"));
        Parent root = loader.load();

        T controller = loader.getController();
        initializer.initialize(controller, stage);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Functional interface for initializing controllers
     * @param <T> The type of the controller
     */
    @FunctionalInterface
    private interface ControllerInitializer<T> {
        void initialize(T controller, Stage stage);
    }
}