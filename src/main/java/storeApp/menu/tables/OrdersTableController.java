package storeApp.menu.tables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import storeApp.orders.Order;
import storeApp.product.Product;

import java.io.IOException;
import java.util.Objects;

import static storeApp.db.OrderDao.*;

/**
 * Controller for the orders table
 * This is a placeholder that can be expanded when order functionality is implemented
 */
public class OrdersTableController extends BaseTableController<Order> {


    @Override
    protected void setupColumns() {
        // Add basic columns that can be expanded later
        TableColumn<Order, String> colOrderId = createColumn("Order ID", "id", 80);
        TableColumn<Order, String> colOrderDate = createColumn("Date", "orderDateFormatted", 70);
        TableColumn<Order, String> colOrderCustomer = createColumn("Customer", "customerName", 120);
        TableColumn<Order, String> colOrderStatus = createColumn("Status", "lastStatusString", 70);
        TableColumn<Order, String> colOrderTrackingNumber = createColumn("Tracking #", "trackingNumber", 80);
        TableColumn<Order, Integer> colOrderItems = createColumn("Items", "itemCount", 50);
        TableColumn<Order, Double> colOrderTotal = createColumn("Total", "total", 80);

        // Action columns
        TableColumn<Order, String> colOrderEdit = createActionColumn("Edit", 40);
        setupEditColumn(colOrderEdit);

        TableColumn<Order, String> colOrderDelete = createActionColumn("Delete", 50);
        setupDeleteColumn(colOrderDelete);


        tableView.getColumns().addAll(
                colOrderId, colOrderDate, colOrderCustomer, colOrderStatus, colOrderTrackingNumber, colOrderItems, colOrderTotal, colOrderEdit, colOrderDelete
        );
    }

    private TableColumn<Order, String> createActionColumn(String title, double width) {
        TableColumn<Order, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        return column;
    }

    private void setupEditColumn(TableColumn<Order, String> column) {
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
                        Order order = getTableView().getItems().get(getIndex());
                        editOrderEntry(order);
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
    private void setupDeleteColumn(TableColumn<Order, String> column) {
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
                        Order order = getTableView().getItems().get(getIndex());
                        deleteOrder(order);
                    });
                    setGraphic(deleteButton);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void editOrderEntry(Order order) {

    }

    private void deleteOrder(Order order) {

    }

    @Override
    public void loadData() {
        dataList.clear();
        ObservableList<Order> orders = getAllOrders();
        dataList.addAll(orders);

        if (dataList.isEmpty()) {
            System.out.println("There are no orders to show.");
        } else {
            System.out.println("Loaded " + dataList.size() + " orders.");
        }
    }

    private <T> void openFormWithController(ControllerInitializer<T> initializer)
            throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Edit Order entry");
        stage.setResizable(false);

        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/product_form.fxml"));
        Parent root = loader.load();

        T controller = loader.getController();
        initializer.initialize(controller, stage);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.showAndWait();
    }

    @FunctionalInterface
    private interface ControllerInitializer<T> {
        void initialize(T controller, Stage stage);
    }
}