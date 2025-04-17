package com.danieljoaco.storeapp.menu.adminMenu;

import com.danieljoaco.storeapp.products.Category;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.products.SubCategory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;

import static com.danieljoaco.storeapp.db.ProductsDao.addProduct;
import static com.danieljoaco.storeapp.menu.utils.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;

public class NewProductController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField refField;

    @FXML
    private TextField costField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockField;

    @FXML
    private TextField billField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> subcategoryComboBox;

    @FXML
    private Label lblError;

    private Stage newProductStage;

    public void initialize(Stage stage) {
        this.newProductStage = stage;

        // Initialize category ComboBox
        categoryComboBox.getItems().addAll(Arrays.stream(Category.Categories.values())
                        .map(category -> capitalize(category.name()))
                        .toList());

        // Add listener for category selection to update subcategories
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
                        .map(item -> capitalize(item))
                        .toList());
                subcategoryComboBox.setPromptText("Select a subcategory");
                showSuccess(lblError, "");
            } catch (IllegalArgumentException e) {
                showError(lblError, "No subcategories found for the selected category.");
            }
        });
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        String name = nameField.getText().toLowerCase();
        String ref = refField.getText().toLowerCase();
        String costText = costField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        String bill = billField.getText().toLowerCase();
        String category = categoryComboBox.getSelectionModel().getSelectedItem();
        String subCategory = subcategoryComboBox.getSelectionModel().getSelectedItem();

        if (subCategory != null) {
            subCategory = subCategory.replace(" ", "_");
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

            try {
                Products newProduct = new Products(name, ref, cost, price, stock, bill, category, subCategory);
                addProduct(newProduct);
            } catch (Exception e) {
                showError(lblError, e.getMessage());
                return;
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

        // Disable the create button
        ((Node) event.getSource()).setDisable(true);

        showSuccess(lblError, name + " product successfully added!");
        boolean continueCreate = askConfirmation("Create another product");
        closeAfterDelay(newProductStage);

        if (continueCreate) {
            newProductStage.close();
            AdminMenu adminMenu = new AdminMenu();
            // Cast the owner to Stage
            adminMenu.createNewProduct((Stage) newProductStage.getOwner());
        }
    }
}