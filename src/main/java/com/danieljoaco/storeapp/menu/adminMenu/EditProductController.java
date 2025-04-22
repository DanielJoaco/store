package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.users.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static com.danieljoaco.storeapp.db.ProductsDao.updateProductReference;
import static com.danieljoaco.storeapp.menu.adminMenu.NewProductController.initializeCategoryComboBox;
import static com.danieljoaco.storeapp.menu.utils.Utils.*;
        import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;

public class EditProductController {
    @FXML
    private TextField nameField, refField;

    @FXML
    private ComboBox<String> categoryComboBox, subcategoryComboBox;

    @FXML
    private Label lblError;

    @FXML
    private Button btnUpdate;

    private Stage editProductStage;
    private ProductReference productToEdit;
    private Admin adminLogin;

    public void initialize(Stage stage, ProductReference productToEdit, Admin adminLogin) {
        this.editProductStage = stage;
        this.productToEdit = productToEdit;
        this.adminLogin = adminLogin;

        initializeCategoryComboBox(this.categoryComboBox, this.subcategoryComboBox, this.lblError);

        // Set the fields with the product information
        nameField.setText(productToEdit.getName());
        refField.setText(productToEdit.getRef());
        categoryComboBox.getItems().addAll(productToEdit.getCategory());
        subcategoryComboBox.getItems().addAll(productToEdit.getSubcategory());

    }

    @FXML
    private void updateProduct() {

        String name = nameField.getText();
        String category = categoryComboBox.getSelectionModel().getSelectedItem();
        String subCategory = subcategoryComboBox.getSelectionModel().getSelectedItem();

        if (category != null) {
            category = category.replace(" ", "_").toUpperCase();
        }
        if (subCategory != null) {
            subCategory = subCategory.replace(" ", "_").toUpperCase();
        }


        // Validate inputs
        if (name.isEmpty()) {
            showError(lblError, "Name cannot be empty");
            return;
        }

        try{
            isValidProductsInputs(name);
        } catch (Exception e) {
            showError(lblError, "Invalid name format");
            return;
        }

        try{
            productToEdit.setName(name, adminLogin);
            updateProductReference(productToEdit);


        }catch (Exception e) {
            showError(lblError, e.getMessage());
            return;
        }

        nameField.setDisable(true);
        categoryComboBox.setDisable(true);
        subcategoryComboBox.setDisable(true);
        btnUpdate.setDisable(true);

        showSuccess(lblError, "Product updated successfully!");
        boolean continueCreate = askConfirmation("Edit another product?");
        closeAfterDelay(editProductStage);

    }
}
