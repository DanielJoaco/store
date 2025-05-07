package com.danieljoaco.storeapp.menu.forms;

import com.danieljoaco.storeapp.menu.tables.OrderProductDetailsTableController;
import com.danieljoaco.storeapp.orders.Order;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class OrderFormController {

    @FXML
    private AnchorPane TableContainer;

    private Order order;
    private List<OrderProductDetailsTableController.OrderProductDetails> orderProductDetailsList;

    public void initialize(Order order) {
        this.order = order;
        orderProductDetailsList = new ArrayList<>();

        for(Order.OrderItem item: order.getItems()){
            OrderProductDetailsTableController.OrderProductDetails orderProductDetails = new OrderProductDetailsTableController.OrderProductDetails(
                    item.productInfo().getRef(),
                    item.productInfo().getName(),
                    item.productInfo().getBrand(),
                    item.quantity(),
                    item.unitPrice()
            );
            orderProductDetailsList.add(orderProductDetails);
        }
        OrderProductDetailsTableController orderProductDetailsTableController = new OrderProductDetailsTableController();
        orderProductDetailsTableController.addTableToContainer(TableContainer);
        orderProductDetailsTableController.initialize(orderProductDetailsList);
    }


}
