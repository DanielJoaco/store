package com.danieljoaco.storeapp.menu.shippingCar;

import com.danieljoaco.storeapp.db.ProductsDao;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ProductCardController {

    @FXML
    private Label lblName, lblQuantity, lblPrice, lblErrorMessage;

    @FXML
    private Button btnReduce, btnAdd;

    @FXML
    private ImageView imageView;

    private ProductsDao.ProductViewInfo productViewInfo;
    private int quantity;
    private double price;
    private ProductCardInfo productCardInfo;

    @FXML
    public void initialize() {
        // Initialize error message label as empty and invisible
        if (lblErrorMessage != null) {
            lblErrorMessage.setText("");
            lblErrorMessage.setVisible(false);
        }
    }

    public void initData(ProductsDao.ProductViewInfo productViewInfo) {
        this.productViewInfo = productViewInfo;
        quantity = 1;
        this.price = productViewInfo.price();

        lblName.setText(productViewInfo.name());
        lblQuantity.setText(String.valueOf(quantity));
        lblPrice.setText(String.format("%.2f", price));

        // Set initial button states
        btnReduce.setDisable(quantity <= 0);
        btnAdd.setDisable(quantity >= productViewInfo.stock());
    }

    @FXML
    public void reduceQuantity() {
        if (quantity > 0) {
            quantity--;
            lblQuantity.setText(String.valueOf(quantity));
            calculatePrice();
            btnReduce.setDisable(quantity <= 0);
            btnAdd.setDisable(false);
        }
    }

    @FXML
    public void addQuantity() {
        if (productViewInfo != null && quantity < productViewInfo.stock()) {
            quantity++;
            lblQuantity.setText(String.valueOf(quantity));
            calculatePrice();
            btnAdd.setDisable(quantity >= productViewInfo.stock());
            btnReduce.setDisable(false);
        }
    }

    private void calculatePrice() {
        double totalPrice = quantity * price;
        lblPrice.setText(String.format("%.2f", totalPrice));
    }

    @FXML
    public void addToCart() {
        if (quantity > 0) {
            this.productCardInfo = new ProductCardInfo(productViewInfo, quantity, price * quantity);

            // Show confirmation message
            lblErrorMessage.setText("Added to cart!");
            lblErrorMessage.setVisible(true);

            // Auto-hide message after 2 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> lblErrorMessage.setVisible(false));
            pause.play();
        } else {
            lblErrorMessage.setText("Please select quantity!");
            lblErrorMessage.setVisible(true);

            // Auto-hide error message after 2 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> lblErrorMessage.setVisible(false));
            pause.play();
        }
    }

    public ProductCardInfo getProductCardInfo() {
        return productCardInfo;
    }

    public void clearProductCardInfo() {
        this.productCardInfo = null;
    }

    public record ProductCardInfo(ProductsDao.ProductViewInfo productViewInfo, int quantity, double total){}
}