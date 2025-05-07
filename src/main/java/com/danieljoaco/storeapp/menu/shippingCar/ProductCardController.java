package com.danieljoaco.storeapp.menu.shippingCar;

import com.danieljoaco.storeapp.db.ProductsDao;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

import static com.danieljoaco.storeapp.menu.utils.Utils.showError;

public class ProductCardController {

    @FXML
    private Label lblName, lblQuantity, lblPrice, lblErrorMessage;

    @FXML
    private Button btnReduce, btnAdd;

    private ProductsDao.ProductViewInfo productViewInfo;
    private int quantity;
    private double price;
    private static List<ProductCardInfo>  productCardInfoList;

    public void initialize() {
        this.productCardInfoList = new ArrayList<>();
    }

    public void initData(ProductsDao.ProductViewInfo productViewInfo) {
        this.productViewInfo = productViewInfo;
        this.quantity = 1;
        this.price = productViewInfo.price();

        lblName.setText(productViewInfo.name());
        lblPrice.setText(String.valueOf(price));
    }

    @FXML
    private void reduceQuantity() {
        if (quantity > 0) {
            btnAdd.setDisable(false);
            quantity--;
            lblQuantity.setText("");
            lblQuantity.setText(String.valueOf(quantity));
            calculatePrice();
        } else if (quantity == 0) {
            btnReduce.setDisable(true);
        }

    }

    @FXML
    public void addQuantity() {
        if (quantity < productViewInfo.stock()) {
            btnReduce.setDisable(false);
            quantity++;
            lblQuantity.setText("");
            lblQuantity.setText(String.valueOf(quantity));
            calculatePrice();
        } else if (quantity == productViewInfo.stock()) {
            btnAdd.setDisable(true);
        }

    }

    private void calculatePrice() {
        double totalPrice = quantity * price;
        lblPrice.setText(String.format("%.2f", totalPrice));
    }


    @FXML
    private void addToCart() {
        ProductCardInfo productCardInfo = new ProductCardInfo(productViewInfo.ref(), quantity, price);

        boolean found = false;
        for (ProductCardInfo p: productCardInfoList) {
            if (p.ref().equals(productCardInfo.ref())) {
                int i = productCardInfoList.indexOf(p);
                productCardInfoList.set(i, productCardInfo);
                found = true;
                break;
            }
        }

        if (!found) {
            productCardInfoList.add(productCardInfo);
        }

    }

    public static List<ProductCardInfo> getProductCardInfoList() {return productCardInfoList;}

    public void viewProductDetails() {
        // Logic to view product details
    }

    public record ProductCardInfo(String ref, int quantity, double price){}
}
