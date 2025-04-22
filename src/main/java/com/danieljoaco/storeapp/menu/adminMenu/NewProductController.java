package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.menu.utils.Utils;
import com.danieljoaco.storeapp.products.Category;
import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.products.SubCategory;
import com.danieljoaco.storeapp.users.Admin;

import static com.danieljoaco.storeapp.db.ProductsDao.addProduct;
import static com.danieljoaco.storeapp.db.ProductsDao.updateProductEntry;
import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Arrays;

public class NewProductController {

    @FXML
    private TextField nameField, brandField, refField, costField, priceField, stockField, billField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> categoryComboBox, subcategoryComboBox;

    @FXML
    private Button createButton;

    @FXML
    private Label lblTittle, lblError;

    private Stage newProductStage;
    private boolean isEditMode = false;
    private Products productToEdit;
    private Admin adminLogin;

    public void initialize(Stage stage, Admin adminLogin) {
        this.newProductStage = stage;
        this.adminLogin = adminLogin;
        initializeCategoryComboBox(this.categoryComboBox, this.subcategoryComboBox, this.lblError);
    }

    public void initialize(Stage stage, Admin adminLogin, ProductReference newProductEntry) {
        initialize(stage, adminLogin);
        parametersNewEntry(newProductEntry);
    }

    public static void initializeCategoryComboBox(ComboBox<String> categoryComboBox, ComboBox<String> subcategoryComboBox, Label lblError) {
        categoryComboBox.getItems().addAll(Arrays.stream(Category.Categories.values())
                .map(category -> Utils.capitalize(category.toString()))
                .toList());

        categoryComboBox.setOnAction(event -> updateSubcategories(categoryComboBox, subcategoryComboBox, lblError));
    }

    private static void updateSubcategories(ComboBox<String> categoryComboBox, ComboBox<String> subcategoryComboBox, Label lblError) {
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
    }

    private void parametersNewEntry(ProductReference newProductEntry) {
        lblTittle.setText("New entry for " + newProductEntry.getName());

        nameField.setText(newProductEntry.getName());
        nameField.setDisable(true);

        brandField.setText(newProductEntry.getBrand());
        brandField.setDisable(true);

        refField.setText(newProductEntry.getRef());
        refField.setDisable(true);

        categoryComboBox.setValue(capitalize(newProductEntry.getCategory()));
        categoryComboBox.setDisable(true);

        subcategoryComboBox.setValue(capitalize(newProductEntry.getSubcategory()));
        subcategoryComboBox.setDisable(true);

        descriptionField.setVisible(false);

        createButton.setText("New entry");
    }

    public void editProduct(Products product) {
        this.isEditMode = true;
        this.productToEdit = product;

        refField.setText(product.getRef());
        refField.setDisable(true);

        nameField.setText(product.getName());
        brandField.setText(product.getBrand());
        stockField.setText(String.valueOf(product.getStock()));
        costField.setText(String.valueOf(product.getCost()));
        priceField.setText(String.valueOf(product.getPrice()));
        billField.setText(product.getBill());
        descriptionField.setText(product.getDescription());

        categoryComboBox.setValue(capitalize(product.getCategory()));
        subcategoryComboBox.setValue(capitalize(product.getSubCategory()));

        createButton.setText("Update Product");
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            // Collect and validate form data
            ProductFormData formData = collectFormData();

            if (isEditMode) {
                updateExistingProduct(formData);
            } else {
                createNewProduct(formData);
            }

            // Disable all fields after success
            disableAllFields();
            showSuccess(lblError, formData.name + " product successfully added!");

            boolean continueCreate = askConfirmation("Create another product?");
            closeAfterDelay(newProductStage);

        } catch (IllegalArgumentException e) {
            showError(lblError, e.getMessage());
        }
    }

    private ProductFormData collectFormData() {
        String name = nameField.getText();
        String brand = brandField.getText();
        String ref = refField.getText();
        String costText = costField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        String bill = billField.getText();
        String description = descriptionField.getText();

        String category = categoryComboBox.getSelectionModel().getSelectedItem();
        String subCategory = subcategoryComboBox.getSelectionModel().getSelectedItem();

        validateInputs(name,  brand, ref, costText, priceText, stockText, bill, category, subCategory, description);

        // Process category and subcategory
        if (category != null) {
            category = category.replace(" ", "_").toUpperCase();
        }
        if (subCategory != null) {
            subCategory = subCategory.replace(" ", "_").toUpperCase();
        }

        double cost = Double.parseDouble(costText);
        double price = Double.parseDouble(priceText);
        int stock = Integer.parseInt(stockText);

        return new ProductFormData(name, brand, ref, cost, price, stock, bill, category, subCategory, description);
    }

    private void validateInputs(String name, String brand, String ref, String costText, String priceText,
                                String stockText, String bill, String category, String subCategory, String description) {
        // Check for empty fields
        if (costText.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            throw new IllegalArgumentException("Cost, price, and stock fields cannot be empty.");
        }

        // Validate numeric fields
        try {
            double cost = Double.parseDouble(costText);
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            if (cost <= 0) {
                throw new IllegalArgumentException("Cost must be greater than 0.");
            }
            if (price <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0.");
            }
            if (stock <= 0) {
                throw new IllegalArgumentException("Stock cannot be negative or zero.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter valid numeric values for cost, price, and stock.");
        }

        // Validate other fields
        if (name.isEmpty() || ref.isEmpty() || bill.isEmpty() || category == null || subCategory == null) {
            throw new IllegalArgumentException("Please fill all the fields.");
        }

        // Validate product inputs format
        try {
            isValidProductsInputs(name);
            isValidProductsInputs(brand);
            isValidProductsInputs(ref);
            isValidProductsInputs(bill);
            isValidProductsInputs(description);
        } catch (Exception e) {
            throw new IllegalArgumentException("Only use alphanumeric characters or _ for name, reference and bill.");
        }
    }

    private void updateExistingProduct(ProductFormData formData) {
        try {
            productToEdit.setName(formData.name, adminLogin);
            productToEdit.setBrand(formData.brand, adminLogin);
            productToEdit.setStock(formData.stock, adminLogin);
            productToEdit.setCost(formData.cost, adminLogin);
            productToEdit.setPrice(formData.price, adminLogin);
            productToEdit.setBill(formData.bill, adminLogin);
            productToEdit.setSubCategory(formData.category, formData.subCategory, adminLogin);
            productToEdit.setDescription(formData.description, adminLogin);

            updateProductEntry(productToEdit);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating product: " + e.getMessage());
        }
    }

    private void createNewProduct(ProductFormData formData) {
        try {
            if (adminLogin.isAdmin()) {
                Products newProduct = new Products(
                        formData.name, formData.brand, formData.ref, formData.cost, formData.price,
                        formData.stock, formData.bill, formData.category, formData.subCategory, formData.description
                );
                addProduct(newProduct);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void disableAllFields() {
        nameField.setDisable(true);
        refField.setDisable(true);
        costField.setDisable(true);
        priceField.setDisable(true);
        stockField.setDisable(true);
        billField.setDisable(true);
        categoryComboBox.setDisable(true);
        subcategoryComboBox.setDisable(true);
        createButton.setDisable(true);
    }

        private record ProductFormData(String name, String brand, String ref, double cost, double price, int stock,
                                       String bill, String category, String subCategory, String description) {}
}