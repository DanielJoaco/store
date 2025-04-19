package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.menu.utils.Utils;
import com.danieljoaco.storeapp.products.Category;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.products.SubCategory;
import com.danieljoaco.storeapp.users.Admin;

import static com.danieljoaco.storeapp.db.ProductsDao.addProduct;
import static com.danieljoaco.storeapp.db.ProductsDao.updateProductEntry;
import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.Arrays;

public class NewProductController {

    @FXML
    private TextField nameField, refField, costField, priceField, stockField, billField;

    @FXML
    private ComboBox<String> categoryComboBox, subcategoryComboBox;

    @FXML
    private Button createButton;

    @FXML
    private Label lblError;

    private Stage newProductStage;
    private boolean isEditMode = false;
    private Products productToEdit;
    private Admin adminLogin;

    public void initialize(Stage stage, Admin adminLogin) {
        this.newProductStage = stage;
        this.adminLogin = adminLogin;

        initializeCategoryComboBox(this.categoryComboBox, this.subcategoryComboBox, this.lblError);
    }

    static void initializeCategoryComboBox(ComboBox<String> categoryComboBox, ComboBox<String> subcategoryComboBox, Label lblError) {
        // Inicializar el ComboBox de categorías
        categoryComboBox.getItems().addAll(Arrays.stream(Category.Categories.values())
                .map(category -> capitalize(category.name()))
                .toList());

        // Agregar listener para actualizar subcategorías al seleccionar una categoría
        categoryComboBox.setOnAction(event -> {
            try {
                String selectedItem = categoryComboBox.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    showError(lblError, "Please select a category");
                    return;
                }

                String selectedCategory = selectedItem.toUpperCase();
                subcategoryComboBox.getItems().clear();
                subcategoryComboBox.getItems().addAll(Arrays.stream(SubCategory.SubCategories.valueOf(selectedCategory).getItems())
                        .map(Utils::capitalize)
                        .toList());
                subcategoryComboBox.setPromptText("Select a subcategory");
                showSuccess(lblError, "");
            } catch (IllegalArgumentException e) {
                showError(lblError, "No subcategories found for the selected category.");
            }
        });
    }

    public void editProduct(Products product) {
        this.isEditMode = true;
        this.productToEdit = product;

        // Pre-cargar los campos con los valores del producto
        refField.setText(product.getRef());
        refField.setDisable(true);
        nameField.setText(product.getName());
        stockField.setText(String.valueOf(product.getStock()));
        costField.setText(String.valueOf(product.getCost()));
        priceField.setText(String.valueOf(product.getPrice()));
        billField.setText(product.getBill());
        categoryComboBox.setValue(capitalize(product.getCategory()));
        subcategoryComboBox.setValue(capitalize(product.getSubCategory()));

        // Cambiar el texto del botón
        createButton.setText("Update Product");
    }

    @FXML
    private void handleCreate(ActionEvent event) {

        String name = nameField.getText();
        String ref = refField.getText();
        String costText = costField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        String bill = billField.getText();
        String category = categoryComboBox.getSelectionModel().getSelectedItem();
        String subCategory = subcategoryComboBox.getSelectionModel().getSelectedItem();

        if (category != null) {
            category = category.replace(" ", "_").toUpperCase();
        }
        if (subCategory != null) {
            subCategory = subCategory.replace(" ", "_").toUpperCase();
        }

        // Validate that fields are not empty
        if (costText.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            showError(lblError, "Cost, price, and stock fields cannot be empty.");
            return;
        }

        double cost, price;
        int stock;

        try {
            cost = Double.parseDouble(costText);
            price = Double.parseDouble(priceText);
            stock = Integer.parseInt(stockText);

            if (cost <= 0) {
                showError(lblError, "Cost must be greater than 0.");
                return;
            }
            if (price <= 0) {
                showError(lblError, "Price must be greater than 0.");
                return;
            }
            if (stock <= 0) {
                showError(lblError, "Stock cannot be negative or zero.");
                return;
            }

        } catch (NumberFormatException e) {
            showError(lblError, "Please enter valid numeric values for cost, price, and stock.");
            return;
        }

        if (name.isEmpty() || ref.isEmpty() || bill.isEmpty() || category == null || subCategory == null) {
            showError(lblError, "Please fill all the fields.");
            return;
        } else {
            try {
                isValidProductsInputs(name);
                isValidProductsInputs(ref);
                isValidProductsInputs(bill);

            } catch (Exception e) {
                showError(lblError, "Only use alphanumeric characters or _ for name, reference and bill" + e.getMessage() + ".");
                return;
            }


            if (isEditMode) {
                // Actualizar producto existente
                productToEdit.setName(name, adminLogin);
                productToEdit.setStock(stock, adminLogin);
                productToEdit.setCost(cost, adminLogin);
                productToEdit.setPrice(price, adminLogin);
                productToEdit.setBill(bill, adminLogin);
                productToEdit.setSubCategory(category, subCategory, adminLogin);

                try {
                    updateProductEntry(productToEdit);

                } catch (Exception e) {
                    showError(lblError, "Error updating product: " + e.getMessage());
                }

            } else {
                try {
                    if (adminLogin.isAdmin()) {
                        Products newProduct = new Products(name, ref, cost, price, stock, bill, category, subCategory);
                        addProduct(newProduct);
                    }
                } catch (Exception e) {
                    showError(lblError, e.getMessage());
                    return;
                }
            }

        }

        // Disable all fields
        nameField.setDisable(true);
        refField.setDisable(true);
        costField.setDisable(true);
        priceField.setDisable(true);
        stockField.setDisable(true);
        billField.setDisable(true);
        categoryComboBox.setDisable(true);
        subcategoryComboBox.setDisable(true);
        createButton.setDisable(true);

        showSuccess(lblError, name + " product successfully added!");
        boolean continueCreate = askConfirmation("Create another product?");
        closeAfterDelay(newProductStage);

    }
}