package com.danieljoaco.storeapp.menu;

import com.danieljoaco.storeapp.products.Category;
import com.danieljoaco.storeapp.products.SubCategory;
import com.danieljoaco.storeapp.products.Products;
import com.danieljoaco.storeapp.users.Admin;

import static com.danieljoaco.storeapp.menu.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;
import static com.danieljoaco.storeapp.db.ProductsDao.*;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Arrays;

public class AdminMenu {

    void showMenu(Admin admin, Stage primaryStage) {

        if (admin.isAdmin()){
            String title = "Admin Menu";
            Stage stageAdminMenu = setupStage(title);
            VBox root = createVBox(30, 20, Pos.CENTER);

            Label lblTitle = createTittleLabel(title, 24);
            Button btnNewProduct = createMenuButton("Agregar nuevo producto", e -> createNewProduct());
            Button btnSetProduct = createMenuButton("Modificar productos", e -> {});
            Button btnDeleteProduct = createMenuButton("Eliminar productos", e -> {});
            Button btnSetUser = createMenuButton("Editar usuario", e -> {});
            Button btnDeleteUser = createMenuButton("Eliminar usuario", e -> {});
            Button btnReturnToMainMenu = createMenuButton("Regresar al menu principal", e -> returnToMainMenu(primaryStage, stageAdminMenu));

            root.getChildren().addAll(lblTitle, btnNewProduct, btnSetProduct, btnDeleteProduct, btnSetUser, btnDeleteUser, btnReturnToMainMenu);
            setScene(stageAdminMenu, root, 500, 600, "/styles/manuMainStyles.css");
            stageAdminMenu.showAndWait();
            
        }
    }
    
    private void createNewProduct() {
        String title = "Nuevo producto";
        Stage stageNewProduct = setupStage(title);
        GridPane newProductGrid = createNewProductsGrid(stageNewProduct, title);
        VBox root = createVBox(15, 20, Pos.CENTER, newProductGrid);
        setScene(stageNewProduct, root, 450, 700, "/styles/manuMainStyles.css");

        stageNewProduct.showAndWait();
    }

    GridPane createNewProductsGrid(Stage newProductStage, String title) {
        GridPane grid = createGridPane();
        addTitleToGrid(grid, title);

        Label lblName = new Label("Name: ");
        TextField txtName = new TextField();

        Label lblRef = new Label("Ref: ");
        TextField txtRef = new TextField();

        Label lblCost = new Label("Cost: ");
        TextField txtCost = new TextField();

        Label lblPrice = new Label("Price: ");
        TextField txtPrice = new TextField();

        Label lblStock = new Label("Stock: ");
        TextField txtStock = new TextField();

        Label lblBill = new Label("Bill: ");
        TextField txtBill = new TextField();

        Label lblCategory = new Label("Category: ");
        ComboBox<String> cbCategory = new ComboBox<>();
        cbCategory.getItems().addAll(Arrays.stream(Category.Categories.values())
                .map(Enum::name)
                .toList());
        cbCategory.setPromptText("Select a category");


        Label lblSubcategory =  new Label("Subcategory: ");
        ComboBox<String> cbSubCategory = new ComboBox<>();

        Label lblError = createErrorLabel();

        cbCategory.setOnAction(event -> {
            try{
                String selectedCategory = cbCategory.getSelectionModel().getSelectedItem();
                cbSubCategory.getItems().clear();
                cbSubCategory.getItems().addAll(Arrays.stream(SubCategory.SubCategories.valueOf(selectedCategory).getItems()).toList());
                cbSubCategory.setPromptText("Select a subcategory");
                showSuccess(lblError, "");
            }catch (Exception e){
                showError(lblError, "Don´t found subcategory for selected category.");
            }

        });

        Button btnCreate = new Button("Create");
        btnCreate.setOnAction(event -> {

            String name = txtName.getText().toLowerCase();
            String ref = txtRef.getText().toLowerCase();
            String costText = txtCost.getText();
            String priceText = txtPrice.getText();
            String stockText = txtStock.getText();
            String bill = txtBill.getText().toLowerCase();
            String category = cbCategory.getSelectionModel().getSelectedItem();
            String subCategory = cbSubCategory.getSelectionModel().getSelectedItem();

            // Validar que los campos no estén vacíos
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

            if(name.isEmpty() || ref.isEmpty()  || bill.isEmpty() || category.isEmpty() || subCategory.isEmpty()){
                showError(lblError, "Please fill all the fields.");
                return;
            } else{
                try {
                    isValidProductsInputs(name);
                    isValidProductsInputs(ref);
                    isValidProductsInputs(bill);
                    Products newProduct = new Products(name, ref, cost, price, stock, bill, category, subCategory);
                    addProduct(newProduct);
                }catch (Exception e){
                    showError(lblError, "Only use alphanumeric characters or _ for name, reference and bill" + e.getMessage() + "." );
                    return;
                }
            }

            Node[] controls = {lblName, txtName, lblRef, txtRef, lblCost, txtCost, lblPrice, txtPrice, lblStock, txtStock, lblBill, txtBill, lblCategory, cbCategory, lblSubcategory, cbSubCategory, btnCreate, lblError};
            disableControls(controls);

            showSuccess(lblError, name + " product successfully added!" );
            closeAfterDelay(newProductStage);
        });


        grid.add(lblName, 0, 2);
        grid.add(txtName, 1, 2);
        grid.add(lblRef, 0, 3);
        grid.add(txtRef, 1, 3);
        grid.add(lblCost, 0, 4);
        grid.add(txtCost, 1, 4);
        grid.add(lblPrice, 0, 5);
        grid.add(txtPrice, 1, 5);
        grid.add(lblStock, 0, 6);
        grid.add(txtStock, 1, 6);
        grid.add(lblBill, 0, 7);
        grid.add(txtBill, 1, 7);
        grid.add(lblCategory, 0, 8);
        grid.add(cbCategory, 1, 8);
        grid.add(lblSubcategory, 0, 9);
        grid.add(cbSubCategory, 1, 9);
        grid.add(btnCreate, 0, 10, 2, 1);
        grid.add(lblError, 0, 11, 2, 1);

        GridPane.setHalignment(btnCreate, HPos.CENTER);
        GridPane.setHalignment(lblError, HPos.CENTER);

        return grid;
    }

}
