package com.danieljoaco.storeapp.menu.forms;

import com.danieljoaco.storeapp.db.ProductsDao;
import com.danieljoaco.storeapp.menu.shippingCar.ProductCardController;
import com.danieljoaco.storeapp.user.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class OrderFormController {

    @FXML
    private Label lblCustomerName;

    @FXML
    private TextField txtSubtotal, txtTax, txtDiscounts, txtTotal;

    private List<ProductCardController.ProductCardInfo> productCardInfoList;
    private Customer.CustomerInfo customer;
    private Stage orderFormStage;
    private double subtotal, tax, discounts, total;


    public void initialize(List<ProductCardController.ProductCardInfo> productCardInfoList, Customer.CustomerInfo customer, Stage orderFormStage) {
        this.productCardInfoList = productCardInfoList;
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

    }

    private void setupTable() {

        //
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (ProductCardController.ProductCardInfo productCardInfo : productCardInfoList) {
            subtotal += productCardInfo.total();
        }
        txtSubtotal.setText(String.valueOf(subtotal));
    }
    private void calculateTotal() {
        tax = Double.parseDouble(txtTax.getText());
        discounts = Double.parseDouble(txtDiscounts.getText());
        total = subtotal + tax - discounts;
        txtTotal.setText(String.valueOf(total));
    }

    @FXML
    private void handleSubmitOrder(){}


}
