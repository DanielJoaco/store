package com.danieljoaco.storeapp.menu.tables;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class OrderProductDetailsTableController extends BaseTableController<OrderProductDetailsTableController.OrderProductDetails> {

    private List<OrderProductDetails> orderProductDetailsList;

    public void initialize(List<OrderProductDetails> orderProductDetailsList) {
        this.orderProductDetailsList = orderProductDetailsList;
        setupColumns();
        loadData();
    }

    @Override
    protected void setupColumns() {
        TableColumn<OrderProductDetails, String> colProductRef = createColumn("Ref", "productRef", 50);
        TableColumn<OrderProductDetails, String> colProductName = createColumn("Name", "productName", 120);
        TableColumn<OrderProductDetails, String> colProductBrand = createColumn("Brand", "productBrand", 60);
        TableColumn<OrderProductDetails, Integer> colQuantity = createQuantityColumn("Quantity", "quantity", 90);
        TableColumn<OrderProductDetails, Double> colPrice = createColumn("Price", "price", 50);

        tableView.getColumns().clear();
        tableView.getColumns().addAll(
                colProductRef, colProductName, colProductBrand, colQuantity, colPrice
        );
    }
    private TableColumn<OrderProductDetails, Integer> createQuantityColumn(String title, String property, double width) {
        TableColumn<OrderProductDetails, Integer> column = new TableColumn<>(title);
        column.setPrefWidth(width);

        column.setCellFactory(col -> new TableCell<>() {
            private final Button minusButton = new Button("-");
            private final Label quantityLabel = new Label();
            private final Button plusButton = new Button("+");
            private final HBox container = new HBox(5);

            {
                container.setAlignment(Pos.CENTER);
                minusButton.setMinWidth(24);
                plusButton.setMinWidth(24);
                container.getChildren().addAll(minusButton, quantityLabel, plusButton);

                minusButton.setOnAction(event -> {
                    OrderProductDetails item = getTableView().getItems().get(getIndex());
                    if (item != null && item.quantity() > 1) {
                        int newQuantity = item.quantity() - 1;
                        updateQuantity(item, newQuantity);
                    }
                });

                plusButton.setOnAction(event -> {
                    OrderProductDetails item = getTableView().getItems().get(getIndex());
                    if (item != null) {
                        int newQuantity = item.quantity() + 1;
                        updateQuantity(item, newQuantity);
                    }
                });
            }

            private void updateQuantity(OrderProductDetails item, int newQuantity) {
                OrderProductDetails updated = new OrderProductDetails(
                        item.productRef(), item.productName(), item.productBrand(),
                        newQuantity, item.price());

                int index = orderProductDetailsList.indexOf(item);
                if (index >= 0) {
                    orderProductDetailsList.set(index, updated);
                    loadData();
                }
            }

            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);

                if (empty || quantity == null) {
                    setGraphic(null);
                } else {
                    quantityLabel.setText(String.valueOf(quantity));
                    setGraphic(container);
                }
            }
        });

        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    @Override
    public void loadData() {
        if (orderProductDetailsList != null) {
            tableView.setItems(FXCollections.observableArrayList(orderProductDetailsList));
        }
    }

    public void updateData(List<OrderProductDetails> newOrderProductDetailsList) {
        this.orderProductDetailsList = newOrderProductDetailsList;
        loadData();
    }

    public List<OrderProductDetails> getOrderProductDetailsList() {return orderProductDetailsList;}

    public record OrderProductDetails(String productRef, String productName, String productBrand, int quantity, double price) {}

    @FunctionalInterface
    private interface ControllerInitializer<C> {
        void initialize(C controller, Stage stage);
    }
}