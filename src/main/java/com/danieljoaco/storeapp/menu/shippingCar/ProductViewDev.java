package com.danieljoaco.storeapp.menu.shippingCar;


import com.danieljoaco.storeapp.Main;
import com.danieljoaco.storeapp.user.Customer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static com.danieljoaco.storeapp.user.UserDao.findUser;
import static com.danieljoaco.storeapp.user.UserDao.findUserByEmail;

public class ProductViewDev extends Main {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el FXML directamente aquí
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product-view.fxml"));
            Parent root = loader.load();

            Customer customer = (Customer) findUserByEmail("qweqwe@sdasdas");
            Customer.CustomerInfo customerInfo = customer.getCustomerInfo();
            // Configurar y mostrar la escena
            Scene scene = new Scene(root);
            primaryStage.setTitle("Store Management Product View (DEV)");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            ProductViewController controller = loader.getController();
            controller.initialize(customerInfo, primaryStage);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting development window: " + e.getMessage());
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
    
    /*
    ProductCardInfo productCardInfo = new ProductCardInfo(productViewInfo, quantity, total);

        boolean found = false;
        String ref;
        for (ProductCardInfo p: productCardInfoList) {
            ref = productCardInfo.productViewInfo.ref();
            if (p.productViewInfo.ref().equals(ref)) {
                int i = productCardInfoList.indexOf(p);
                productCardInfoList.set(i, productCardInfo);
                found = true;
                break;
            }
        }

        if (!found) {
            productCardInfoList.add(productCardInfo);
        }
     */
}
