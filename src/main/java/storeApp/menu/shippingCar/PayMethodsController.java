package storeApp.menu.shippingCar;

import storeApp.orders.Payment;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static storeApp.menu.utils.Utils.capitalize;
import static storeApp.menu.utils.Utils.showError;

public class PayMethodsController {

    @FXML
    private ComboBox<String> comboBoxMethods, comboBoxFranchises;

    @FXML
    private Label lblFranchises, lblError;

    private Stage stage;
    private String selectedMethod;
    private String selectedFranchise;
    private Payment.PaymentData paymentData;

    public void initialize(Stage stage) {
        this.stage = stage;
        this.selectedMethod = "";

        comboBoxMethods.getItems().addAll(Payment.PaymentMethod.getAllMethods());
        comboBoxFranchises.getItems().addAll(Payment.Franchises.getAllFranchises());

        comboBoxMethods.setOnAction(event -> {
            selectedMethod = comboBoxMethods.getValue();
            lblFranchises.setVisible(selectedMethod.equals(capitalize(Payment.PaymentMethod.CREDIT_CARD.name())) || selectedMethod.equals(capitalize(Payment.PaymentMethod.DEBIT_CARD.name())));
            comboBoxFranchises.setVisible(lblFranchises.isVisible());
        });
        comboBoxFranchises.setOnAction(event -> {
            selectedFranchise = comboBoxFranchises.getValue();
        });
    }

    @FXML
    private void createOrder() {
        if(selectedMethod.isEmpty()){
            showError(lblError, "Please select a payment method.");
        }else{
            try {
                String method = selectedMethod.toUpperCase().replace(" ", "_");
                String franchise = selectedFranchise == null ? null : selectedFranchise.toUpperCase().replace(" ", "_");
                paymentData = new Payment.PaymentData(
                        Payment.PaymentMethod.valueOf(method),
                        franchise == null ? null : Payment.Franchises.valueOf(franchise)
                );
                stage.close();
            } catch (IllegalArgumentException e) {
                showError(lblError, "Invalid payment method or franchise.");
            }
        }
    }

    public Payment.PaymentData getPaymentData() {return paymentData;}
}
