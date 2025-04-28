package com.danieljoaco.storeapp.menu.forms;

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

public class ProductFormController extends FormController {

    @FXML
    private TextField txtName, txtBrand, txtRef, txtCost, txtPrice, txtStock, txtBill;

    @FXML
    private TextArea txtDescription;

    @FXML
    private ComboBox<String> categoryComboBox, subcategoryComboBox;

    @FXML
    private Button btnSubmit;

    @FXML
    private Label lblTitle, lblCost, lblPrice, lblStock, lblBill, lblCategory, lblSubcategory, lblDescription, lblError;

    private Products productToEdit;
    private ProductReference referenceToEdit;

    private enum ProductFormMode {
        NEW_PRODUCT,         // Creating a completely new product
        NEW_ENTRY,           // Adding a new entry for existing product reference
        EDIT_PRODUCT,        // Editing a full product
        EDIT_PRODUCT_DATA    // Editing only product reference details
    }

    private ProductFormMode productMode;

    /**
     * Initializes the controller with the given stage and admin login for new product creation.
     */
    @Override
    public void initialize(Stage stage, Admin adminLogin) {
        super.initialize(stage, adminLogin);
        this.productMode = ProductFormMode.NEW_PRODUCT;
        this.setErrorLabel(lblError);
        initializeCategoryComboBox();

        lblTitle.setText("Create New Product");
        btnSubmit.setText("Create Product");
    }

    /**
     * Initializes the controller for creating a new entry of an existing product reference.
     */
    public void initializeForNewEntry(Stage stage, Admin adminLogin, ProductReference productRef) {
        initialize(stage, adminLogin);
        this.productMode = ProductFormMode.NEW_ENTRY;

        lblTitle.setText("New entry for " + productRef.getName());
        btnSubmit.setText("Add Entry");

        // Pre-fill and disable fields from the reference
        txtName.setText(productRef.getName());
        txtName.setDisable(true);

        txtBrand.setText(productRef.getBrand());
        txtBrand.setDisable(true);

        txtRef.setText(productRef.getRef());
        txtRef.setDisable(true);

        categoryComboBox.setValue(capitalize(productRef.getCategory()));
        categoryComboBox.setDisable(true);

        subcategoryComboBox.setValue(capitalize(productRef.getSubcategory()));
        subcategoryComboBox.setDisable(true);

        // Hide description as it's not needed for new entry
        lblDescription.setVisible(false);
        txtDescription.setVisible(false);
    }

    /**
     * Initializes the controller for editing a full product.
     */
    public void initializeForEditProduct(Stage stage, Admin adminLogin, Products product) {
        initialize(stage, adminLogin);
        this.productMode = ProductFormMode.EDIT_PRODUCT;
        this.productToEdit = product;

        lblTitle.setText("Edit Product");
        btnSubmit.setText("Update Product");

        // Fill all fields
        txtName.setText(product.getName());
        txtBrand.setText(product.getBrand());
        txtRef.setText(product.getRef());
        txtRef.setDisable(true);  // Ref can't be changed
        txtStock.setText(String.valueOf(product.getStock()));
        txtCost.setText(String.valueOf(product.getCost()));
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtBill.setText(product.getBill());
        txtDescription.setText(product.getDescription());

        categoryComboBox.setValue(capitalize(product.getCategory()));
        subcategoryComboBox.setValue(capitalize(product.getSubCategory()));
    }

    /**
     * Initializes the controller for editing only product reference details.
     */
    public void initializeForEditProdData(Stage stage, Admin adminLogin, ProductReference reference) {
        initialize(stage, adminLogin);
        this.productMode = ProductFormMode.EDIT_PRODUCT_DATA;
        this.referenceToEdit = reference;

        lblTitle.setText("Edit Product Reference");

        // Fill reference fields
        txtName.setText(reference.getName());
        txtBrand.setText(reference.getBrand());
        txtRef.setText(reference.getRef());
        txtRef.setDisable(true);  // Ref can't be changed

        categoryComboBox.setValue(capitalize(reference.getCategory()));
        GridPane.setRowIndex(lblCategory, 5);
        GridPane.setRowIndex(categoryComboBox, 5);

        subcategoryComboBox.setValue(capitalize(reference.getSubcategory()));
        GridPane.setRowIndex(lblSubcategory, 6);
        GridPane.setRowIndex(subcategoryComboBox, 6);

        txtDescription.setText(reference.getDescription());
        GridPane.setRowIndex(lblDescription, 7);
        GridPane.setRowIndex(txtDescription, 7);

        btnSubmit.setText("Update Reference");
        GridPane.setRowIndex(btnSubmit, 8);

        GridPane.setRowIndex(lblError, 9);

        // Hide fields not relevant for references
        lblCost.setVisible(false);
        txtCost.setVisible(false);
        lblPrice.setVisible(false);
        txtPrice.setVisible(false);
        lblStock.setVisible(false);
        txtStock.setVisible(false);
        lblBill.setVisible(false);
        txtBill.setVisible(false);
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
                showFormError("Please select a category");
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
            showFormError("No subcategories found for the selected category.");
        }
    }

    /**
     * Handles the submit action based on the current form mode.
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        try {
            handleSubmit();
        } catch (IllegalArgumentException e) {
            showFormError(e.getMessage());
        }
    }

    /**
     * Implementation of abstract method to handle submission.
     */
    @Override
    protected void handleSubmit() {
        switch (productMode) {
            case NEW_PRODUCT -> handleNewProduct();
            case NEW_ENTRY -> handleNewEntry();
            case EDIT_PRODUCT -> handleEditProduct();
            case EDIT_PRODUCT_DATA -> handleEditProdData();
        }
    }

    /**
     * Handles creating a brand new product.
     */
    private void handleNewProduct() {
        ProductFormData formData = collectFormData(true);
        createNewProduct(formData);
        showSuccess(lblError, formData.name + " product successfully added!");

        processContinueCreating("product");
    }

    /**
     * Handles creating a new entry for an existing product reference.
     */
    private void handleNewEntry() {
        ProductFormData formData = collectFormData(true);
        createNewProduct(formData);
        showSuccessAndClose(formData.name + " product successfully added!");
    }

    /**
     * Handles editing an existing product.
     */
    private void handleEditProduct() {
        ProductFormData formData = collectFormData(true);
        updateExistingProduct(formData);
        showSuccessAndClose(formData.name + " product successfully updated!");
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

            showSuccessAndClose("Product reference updated successfully!");
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Implementation of abstract method to validate form data.
     */
    @Override
    protected boolean validateFormData() {
        try {
            boolean validateFullProduct = productMode != ProductFormMode.EDIT_PRODUCT_DATA;
            collectFormData(validateFullProduct);
            return true;
        } catch (IllegalArgumentException e) {
            showFormError(e.getMessage());
            return false;
        }
    }

    /**
     * Collects the data from the input fields and validates them.
     *
     * @param validateFullProduct if true, validates all fields including cost, price, stock, bill
     * @return a ProductFormData object containing the collected data
     */
    private ProductFormData collectFormData(boolean validateFullProduct) {
        String name = txtName.getText();
        String brand = txtBrand.getText();
        String ref = txtRef.getText();
        String description = txtDescription.getText();

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
        String costText = txtCost.getText();
        String priceText = txtPrice.getText();
        String stockText = txtStock.getText();
        String bill = txtBill.getText();

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
     * Implementation of abstract method to reset all input fields
     */
    @Override
    protected void resetFields() {
        txtName.clear();
        txtName.setDisable(false);

        txtBrand.clear();
        txtBrand.setDisable(false);

        txtRef.clear();
        txtRef.setDisable(false);

        txtCost.clear();
        txtPrice.clear();
        txtStock.clear();
        txtBill.clear();
        txtDescription.clear();

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
     * Implementation of abstract method to disable all fields.
     */
    @Override
    protected void disableAllFields() {
        txtName.setDisable(true);
        txtBrand.setDisable(true);
        txtRef.setDisable(true);

        // Only disable if visible/available
        if (txtCost != null && txtCost.isVisible()) txtCost.setDisable(true);
        if (txtPrice != null && txtPrice.isVisible()) txtPrice.setDisable(true);
        if (txtStock != null && txtStock.isVisible()) txtStock.setDisable(true);
        if (txtBill != null && txtBill.isVisible()) txtBill.setDisable(true);

        txtDescription.setDisable(true);
        categoryComboBox.setDisable(true);
        subcategoryComboBox.setDisable(true);
        btnSubmit.setDisable(true);
    }

    /**
     * Record that stores the data entered by the user.
     */
    private record ProductFormData(String name, String brand, String ref, double cost, double price, int stock,
                                   String bill, String category, String subCategory, String description) {}
}