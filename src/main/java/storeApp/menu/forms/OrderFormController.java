package storeApp.menu.forms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import storeApp.menu.shippingCar.PayMethodsController;
import storeApp.menu.shippingCar.ProductCardController;
import storeApp.menu.tables.ViewProductsOrderTable;
import storeApp.orders.Address;
import storeApp.orders.Order;
import storeApp.orders.OrderService;
import storeApp.orders.Payment;
import storeApp.user.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.List;

import static storeApp.db.OrderDao.createOrder;

public class OrderFormController {

    @FXML
    private Label lblCustomerName;

    @FXML
    private TextField txtSubtotal, txtTax, txtDiscounts, txtTotal;

    @FXML
    private AnchorPane tableProductsOrder;

    private List<Order.OrderItem> listOrderItems;
    private Customer.CustomerInfo customer;
    private Stage orderFormStage;
    private double subtotal, tax, discounts, total;
    private ViewProductsOrderTable viewProductsOrderTable;


    public void initialize(List<Order.OrderItem> listOrderItems, Customer.CustomerInfo customer, Stage orderFormStage) {
        this.listOrderItems = listOrderItems;
        this.customer = customer;
        this.orderFormStage = orderFormStage;
        this.lblCustomerName.setText(customer.name());
        this.txtSubtotal.setDisable(true);
        this.txtTax.setText("0");
        this.txtDiscounts.setText("0");
        this.txtTotal.setDisable(true);

        setupTable();
        calculateSubtotal();
        calculateTotal();

        txtTax.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                double tax = Double.parseDouble(newValue);
                boolean isValid = tax >= 0 && tax <= 100;
                if(isValid){
                    txtTax.setStyle("-fx-text-fill: black;");
                    calculateTotal();
                } else{
                    txtTax.setStyle("-fx-text-fill: red;");
                }

                }
        });

        txtDiscounts.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                double discounts = Double.parseDouble(newValue);
                double parcialTotal = subtotal + subtotal * tax / 100;
                boolean isValid = discounts >= 0 && discounts <= parcialTotal;
                if(isValid){
                    txtDiscounts.setStyle("-fx-text-fill: black;");
                    calculateTotal();
                } else{
                    txtDiscounts.setStyle("-fx-text-fill: red;");
                }
            }
        });

    }

    private void setupTable() {

        this.viewProductsOrderTable = new ViewProductsOrderTable(listOrderItems);
        viewProductsOrderTable.createTable();
        viewProductsOrderTable.addTableToContainer(tableProductsOrder);
        viewProductsOrderTable.loadData();
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (Order.OrderItem product : listOrderItems) {
            subtotal += product.quantity() * product.unitPrice();
        }
        txtSubtotal.setText(String.valueOf(subtotal));
    }
    private void calculateTotal() {
        tax = Double.parseDouble(txtTax.getText());
        discounts = Double.parseDouble(txtDiscounts.getText());
        if(tax>0) total = (subtotal + subtotal*tax/100) - discounts;
        else total = subtotal - discounts;
        txtTotal.setText(String.valueOf(total));
    }

    @FXML
    private void handleSubmitOrder(){

        Payment.PaymentData paymentData = paymentDataForm();
        if(paymentData == null){
            return;
        }
        Address shippingAddress = addressForm();
        if (shippingAddress == null) {
            return;
        }else{
            Order order = OrderService.createOrder(
                    customer,
                    listOrderItems,
                    paymentData,
                    0,
                    tax,
                    discounts,
                    shippingAddress,
                    null
            );
            boolean createdOrder = createOrder(order);
            if(createdOrder){
                orderFormStage.close();

            }else{
                System.out.println("Error creating order");
            }
        }

    }

    private Payment.PaymentData paymentDataForm() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pay-methods.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);
        Stage paymentStage = new Stage();
        paymentStage.setTitle("Payment Methods");
        paymentStage.setScene(scene);
        paymentStage.setResizable(false);

        PayMethodsController payMethodsController = loader.getController();
        payMethodsController.initialize(paymentStage);

        paymentStage.showAndWait();

        return payMethodsController.getPaymentData();
    }

    private Address addressForm() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/address-form.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);
        Stage addressStage = new Stage();
        addressStage.setTitle("Shipping Address");
        addressStage.setScene(scene);
        addressStage.setResizable(false);

        AddressFormController addressFormController = loader.getController();
        addressFormController.initialize(addressStage);

        addressStage.showAndWait();

        return addressFormController.getAddress();
    }

}
