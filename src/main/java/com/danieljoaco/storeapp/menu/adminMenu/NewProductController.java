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
    private Label lblTittle, lblDescription, lblError;

    private Stage newProductStage;
    private boolean isEditEntry;
    private boolean isNewEntry;
    private Products productToEdit;
    private Admin adminLogin;

    /**
     * Initializes the controller with the given stage and admin login.
     *
     * @param stage the stage to be used for the new product entry
     * @param adminLogin the admin login information
     */
    public void initialize(Stage stage, Admin adminLogin) {
        this.newProductStage = stage;
        this.adminLogin = adminLogin;
        this.isNewEntry = false;
        this.isEditEntry = false;
        initializeCategoryComboBox(this.categoryComboBox, this.subcategoryComboBox, this.lblError);
    }

    /**
     * Initializes the controller with the given stage, admin login, and product reference for a new entry.
     *
     * @param stage the stage to be used for the new product entry
     * @param adminLogin the admin login information
     * @param newProductEntry the product reference for the new entry
     */
    public void initialize(Stage stage, Admin adminLogin, ProductReference newProductEntry) {
        initialize(stage, adminLogin);
        parametersNewEntry(newProductEntry);
    }

    /**
     * Initializes the category combo box and sets up the action listener for it.
     *
     * @param categoryComboBox the category combo box to be initialized
     * @param subcategoryComboBox the subcategory combo box to be updated based on the selected category
     * @param lblError the label to display error messages
     */
    public static void initializeCategoryComboBox(ComboBox<String> categoryComboBox, ComboBox<String> subcategoryComboBox, Label lblError) {
        categoryComboBox.getItems().addAll(Arrays.stream(Category.Categories.values())
                .map(category -> Utils.capitalize(category.toString()))
                .toList());

        categoryComboBox.setOnAction(event -> updateSubcategories(categoryComboBox, subcategoryComboBox, lblError));
    }

    /**
     * Updates the subcategory combo box based on the selected category.
     *
     * @param categoryComboBox the category combo box
     * @param subcategoryComboBox the subcategory combo box to be updated
     * @param lblError the label to display error messages
     */
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

    /**
     * Sets the parameters for a new product entry.
     *
     * @param newProductEntry the product reference for the new entry
     */
    private void parametersNewEntry(ProductReference newProductEntry) {
        this.isNewEntry = true;

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

        lblDescription.setVisible(false);
        descriptionField.setVisible(false);

        createButton.setText("New entry");
    }

    /**
     * Sets the parameters for editing an existing product entry.
     *
     * @param product the product entry to be edited
     */
    public void editProductEntry(Products product) {
        this.isEditEntry = true;
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

    /**
     * Handles the action of creating or updating a product entry.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            ProductFormData formData = collectFormData();

            if (isEditEntry) {
                updateExistingProduct(formData);
                disableAllFields();
                showSuccess(lblError, formData.name + " product successfully updated!");
                closeAfterDelay(newProductStage);
            } else if (isNewEntry) {
                createNewProduct(formData);
                disableAllFields();
                showSuccess(lblError, formData.name + " product successfully added!");
                closeAfterDelay(newProductStage);
            } else {
                // Creating a brand new product
                createNewProduct(formData);
                showSuccess(lblError, formData.name + " product successfully added!");

                // Ask if user wants to create another product
                boolean continueCreate = askConfirmation("Create another product?");

                if (continueCreate) {
                    // Reset fields for a new product entry
                    resetFields();
                } else {
                    // User doesn't want to continue, disable fields and close
                    disableAllFields();
                    closeAfterDelay(newProductStage);
                }
            }

        } catch (IllegalArgumentException e) {
            showError(lblError, e.getMessage());
        }
    }

    /**
     * Resets all input fields to prepare for creating a new product
     */
    private void resetFields() {
        nameField.clear();
        nameField.setDisable(false);

        brandField.clear();
        brandField.setDisable(false);

        refField.clear();
        refField.setDisable(false);

        costField.clear();
        priceField.clear();
        stockField.clear();
        billField.clear();
        descriptionField.clear();

        // Reset category and subcategory fields
        categoryComboBox.setValue(null);
        categoryComboBox.setDisable(false);

        subcategoryComboBox.getItems().clear();
        subcategoryComboBox.setValue(null);
        subcategoryComboBox.setDisable(false);

        // Enable create button (just in case)
        createButton.setDisable(false);

        // Clear any success/error message
        lblError.setText("");
    }

    /**
     * Collects the data from the input fields and validates them.
     *
     * @return a ProductFormData object containing the collected data
     */
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

    /**
     * Validates the input fields for creating or updating a product.
     *
     * @param name the name of the product
     * @param brand the brand of the product
     * @param ref the reference of the product
     * @param costText the cost of the product as a string
     * @param priceText the price of the product as a string
     * @param stockText the stock of the product as a string
     * @param bill the bill of the product
     * @param category the category of the product
     * @param subCategory the subcategory of the product
     * @param description the description of the product
     */
    private void validateInputs(String name, String brand, String ref, String costText, String priceText,
                                String stockText, String bill, String category, String subCategory, String description) {
        if (costText.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            throw new IllegalArgumentException("Cost, price, and stock fields cannot be empty.");
        }

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

        if (name.isEmpty() || ref.isEmpty() || bill.isEmpty() || category == null || subCategory == null) {
            throw new IllegalArgumentException("Please fill all the fields.");
        }

        try {
            isValidProductsInputs(name);
            isValidProductsInputs(brand);
            isValidProductsInputs(ref);
            isValidProductsInputs(bill);
            isValidProductsInputs(description);
            int maxLengthDescription = 550;
            if(description.length() > maxLengthDescription) {
                throw new IllegalArgumentException(String.format("Description cannot exceed %d characters.", maxLengthDescription));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Only use alphanumeric characters or _ for name, brand, reference, description and bill.");
        }
    }

    /**
     * Updates an existing product entry with the new data.
     *
     * @param formData the new product data
     */
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

    /**
     * Creates a new product entry with the given data.
     *
     * @param formData the new product data
     */
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

    /**
     * Disable all fields.
     */
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


    /**
     *Record that stores the data entered by the user.
     */
    private record ProductFormData(String name, String brand, String ref, double cost, double price, int stock,
                                       String bill, String category, String subCategory, String description) {}
}