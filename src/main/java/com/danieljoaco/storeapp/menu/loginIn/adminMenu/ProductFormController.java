package com.danieljoaco.storeapp.menu.loginIn.adminMenu;

import com.danieljoaco.storeapp.menu.utils.Utils;
import com.danieljoaco.storeapp.products.Category;
import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.products.SubCategory;
import com.danieljoaco.storeapp.users.Admin;

import static com.danieljoaco.storeapp.db.ProductsDao.*;
import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.Arrays;

public class ProductFormController {

    @FXML
    private TextField nameField, brandField, refField, costField, priceField, stockField, billField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> categoryComboBox, subcategoryComboBox;

    @FXML
    private Button submitButton;

    @FXML
    private Label lblTitle, lblCost, lblPrice, lblStock, lblBill, lblCategory, lblSubcategory, lblDescription, lblError;

    private Stage stage;
    private Admin adminLogin;

    // Form operation modes
    private enum FormMode {
        NEW_PRODUCT,         // Creating a completely new product
        NEW_ENTRY,           // Adding a new entry for existing product reference
        EDIT_PRODUCT,        // Editing a full product
        EDIT_PRODUCT_DATA       // Editing only product reference details
    }

    private FormMode currentMode;
    private Products productToEdit;          // For editing a full product
    private ProductReference referenceToEdit; // For editing just a reference

    /**
     * Initializes the controller with the given stage and admin login for new product creation.
     */
    public void initialize(Stage stage, Admin adminLogin) {
        this.stage = stage;
        this.adminLogin = adminLogin;
        this.currentMode = FormMode.NEW_PRODUCT;
        initializeCategoryComboBox();

        lblTitle.setText("Create New Product");
        submitButton.setText("Create Product");
    }

    /**
     * Initializes the controller for creating a new entry of an existing product reference.
     */
    public void initializeForNewEntry(Stage stage, Admin adminLogin, ProductReference productRef) {
        initialize(stage, adminLogin);
        this.currentMode = FormMode.NEW_ENTRY;

        lblTitle.setText("New entry for " + productRef.getName());
        submitButton.setText("Add Entry");

        // Pre-fill and disable fields from the reference
        nameField.setText(productRef.getName());
        nameField.setDisable(true);

        brandField.setText(productRef.getBrand());
        brandField.setDisable(true);

        refField.setText(productRef.getRef());
        refField.setDisable(true);

        categoryComboBox.setValue(capitalize(productRef.getCategory()));
        categoryComboBox.setDisable(true);

        subcategoryComboBox.setValue(capitalize(productRef.getSubcategory()));
        subcategoryComboBox.setDisable(true);

        // Hide description as it's not needed for new entry
        lblDescription.setVisible(false);
        descriptionField.setVisible(false);
    }

    /**
     * Initializes the controller for editing a full product.
     */
    public void initializeForEditProduct(Stage stage, Admin adminLogin, Products product) {
        initialize(stage, adminLogin);
        this.currentMode = FormMode.EDIT_PRODUCT;
        this.productToEdit = product;

        lblTitle.setText("Edit Product");
        submitButton.setText("Update Product");

        // Fill all fields
        nameField.setText(product.getName());
        brandField.setText(product.getBrand());
        refField.setText(product.getRef());
        refField.setDisable(true);  // Ref can't be changed
        stockField.setText(String.valueOf(product.getStock()));
        costField.setText(String.valueOf(product.getCost()));
        priceField.setText(String.valueOf(product.getPrice()));
        billField.setText(product.getBill());
        descriptionField.setText(product.getDescription());

        categoryComboBox.setValue(capitalize(product.getCategory()));
        subcategoryComboBox.setValue(capitalize(product.getSubCategory()));
    }

    /**
     * Initializes the controller for editing only product reference details.
     */
    public void initializeForEditProdData(Stage stage, Admin adminLogin, ProductReference reference) {
        initialize(stage, adminLogin);
        this.currentMode = FormMode.EDIT_PRODUCT_DATA;
        this.referenceToEdit = reference;

        lblTitle.setText("Edit Product Reference");


        // Fill reference fields
        nameField.setText(reference.getName());
        brandField.setText(reference.getBrand());
        refField.setText(reference.getRef());
        refField.setDisable(true);  // Ref can't be changed


        categoryComboBox.setValue(capitalize(reference.getCategory()));
        GridPane.setRowIndex(lblCategory, 5);
        GridPane.setRowIndex(categoryComboBox, 5);

        subcategoryComboBox.setValue(capitalize(reference.getSubcategory()));
        GridPane.setRowIndex(lblSubcategory, 6);
        GridPane.setRowIndex(subcategoryComboBox, 6);

        descriptionField.setText(reference.getDescription());
        GridPane.setRowIndex(lblDescription, 7);
        GridPane.setRowIndex(descriptionField, 7);

        submitButton.setText("Update Reference");
        GridPane.setRowIndex(submitButton, 8);

        GridPane.setRowIndex(lblError, 9);

        // Hide fields not relevant for references
        lblCost.setVisible(false);
        costField.setVisible(false);
        lblPrice.setVisible(false);
        priceField.setVisible(false);
        lblStock.setVisible(false);
        stockField.setVisible(false);
        lblBill.setVisible(false);
        billField.setVisible(false);
    }

    /**
     * Initializes the category combo box and sets up the action listener for it.
     */
    private void initializeCategoryComboBox() {
        categoryComboBox.getItems().addAll(Arrays.stream(Category.Categories.values())
                .map(category -> Utils.capitalize(category.toString()))
                .toList());

        categoryComboBox.setOnAction(event -> updateSubcategories());
    }

    /**
     * Updates the subcategory combo box based on the selected category.
     */
    private void updateSubcategories() {
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
     * Handles the submit action based on the current form mode.
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        try {
            switch (currentMode) {
                case NEW_PRODUCT -> handleNewProduct();
                case NEW_ENTRY -> handleNewEntry();
                case EDIT_PRODUCT -> handleEditProduct();
                case EDIT_PRODUCT_DATA -> handleEditProdData();
            }
        } catch (IllegalArgumentException e) {
            showError(lblError, e.getMessage());
        }
    }

    /**
     * Handles creating a brand new product.
     */
    private void handleNewProduct() {
        ProductFormData formData = collectFormData(true);
        createNewProduct(formData);
        showSuccess(lblError, formData.name + " product successfully added!");

        boolean continueCreate = askConfirmation("Create another product?");

        if (continueCreate) {
            resetFields();
        } else {
            disableAllFields();
            closeAfterDelay(stage);
        }
    }

    /**
     * Handles creating a new entry for an existing product reference.
     */
    private void handleNewEntry() {
        ProductFormData formData = collectFormData(true);
        createNewProduct(formData);
        disableAllFields();
        showSuccess(lblError, formData.name + " product successfully added!");
        closeAfterDelay(stage);
    }

    /**
     * Handles editing an existing product.
     */
    private void handleEditProduct() {
        ProductFormData formData = collectFormData(true);
        updateExistingProduct(formData);
        disableAllFields();
        showSuccess(lblError, formData.name + " product successfully updated!");
        closeAfterDelay(stage);
    }

    /**
     * Handles editing an existing product reference.
     */
    private void handleEditProdData() {
        ProductFormData formData = collectFormData(false);

        try {
            referenceToEdit.setName(formData.name, adminLogin);
            referenceToEdit.setBrand(formData.brand, adminLogin);
            referenceToEdit.setCategory(formData.category, adminLogin);
            referenceToEdit.setSubcategory(formData.subCategory, adminLogin);
            referenceToEdit.setDescription(formData.description, adminLogin);
            updateProductReference(referenceToEdit);

            disableAllFields();
            showSuccess(lblError, "Product reference updated successfully!");
            closeAfterDelay(stage);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Collects the data from the input fields and validates them.
     *
     * @param validateFullProduct if true, validates all fields including cost, price, stock, bill
     * @return a ProductFormData object containing the collected data
     */
    private ProductFormData collectFormData(boolean validateFullProduct) {
        String name = nameField.getText();
        String brand = brandField.getText();
        String ref = refField.getText();
        String description = descriptionField.getText();

        String category = categoryComboBox.getSelectionModel().getSelectedItem();
        String subCategory = subcategoryComboBox.getSelectionModel().getSelectedItem();

        // Process category and subcategory
        if (category != null) {
            category = category.replace(" ", "_").toUpperCase();
        }
        if (subCategory != null) {
            subCategory = subCategory.replace(" ", "_").toUpperCase();
        }


        if(name.isEmpty() || brand.isEmpty() || ref.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("Please fill all required fields.");
        }

        // Validate common fields
        validateBasicInputs(name, brand, ref, category, subCategory, description);

        // Return early for reference-only operations
        if (!validateFullProduct) {
            return new ProductFormData(name, brand, ref, 0, 0, 0, null, category, subCategory, description);
        }

        // For full product, validate additional fields
        String costText = costField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        String bill = billField.getText();

        validateProductInputs(costText, priceText, stockText, bill);

        double cost = Double.parseDouble(costText);
        double price = Double.parseDouble(priceText);
        int stock = Integer.parseInt(stockText);

        return new ProductFormData(name, brand, ref, cost, price, stock, bill, category, subCategory, description);
    }

    /**
     * Validates the basic input fields common to all operations.
     */
    private void validateBasicInputs(String name, String brand, String ref,
                                     String category, String subCategory, String description) {
        if (name.isEmpty() || ref.isEmpty() || category == null || subCategory == null) {
            throw new IllegalArgumentException("Please fill all required fields.");
        }

        try {
            isValidProductsInputs(name);
            isValidProductsInputs(brand);
            isValidProductsInputs(ref);
            isValidProductsInputs(description);

            int maxLengthDescription = 550;
            if(description.length() > maxLengthDescription) {
                throw new IllegalArgumentException(String.format("Description cannot exceed %d characters.", maxLengthDescription));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Only use alphanumeric characters or _ for name, brand, reference and description.");
        }
    }

    /**
     * Validates the product-specific input fields.
     */
    private void validateProductInputs(String costText, String priceText, String stockText, String bill) {
        if (costText.isEmpty() || priceText.isEmpty() || stockText.isEmpty() || bill.isEmpty()) {
            throw new IllegalArgumentException("Cost, price, stock and bill fields cannot be empty.");
        }

        try {
            double cost = Double.parseDouble(costText);
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            isValidProductsInputs(bill);

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
        } catch (Exception e) {
            throw new IllegalArgumentException("Only use alphanumeric characters or _ for bill.");
        }
    }

    /**
     * Updates an existing product entry with the new data.
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

        // Clear any success/error message
        lblError.setText("");
    }

    /**
     * Disable all fields.
     */
    private void disableAllFields() {
        nameField.setDisable(true);
        brandField.setDisable(true);
        refField.setDisable(true);

        // Only disable if visible/available
        if (costField != null && costField.isVisible()) costField.setDisable(true);
        if (priceField != null && priceField.isVisible()) priceField.setDisable(true);
        if (stockField != null && stockField.isVisible()) stockField.setDisable(true);
        if (billField != null && billField.isVisible()) billField.setDisable(true);

        descriptionField.setDisable(true);
        categoryComboBox.setDisable(true);
        subcategoryComboBox.setDisable(true);
        submitButton.setDisable(true);
    }

    /**
     * Record that stores the data entered by the user.
     */
    private record ProductFormData(String name, String brand, String ref, double cost, double price, int stock,
                                   String bill, String category, String subCategory, String description) {}
}