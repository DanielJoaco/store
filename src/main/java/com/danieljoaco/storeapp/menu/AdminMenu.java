package com.danieljoaco.storeapp.menu;

import com.danieljoaco.storeapp.products.*;
import com.danieljoaco.storeapp.users.Admin;
import com.danieljoaco.storeapp.utils.InputField;

import static com.danieljoaco.storeapp.menu.Utils.*;
import static com.danieljoaco.storeapp.utils.UserValidator.isValidProductsInputs;
import static com.danieljoaco.storeapp.db.ProductsDao.*;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AdminMenu {

    void showMenu(Admin admin, Stage primaryStage) {

        if (admin.isAdmin()){
            String title = "Admin Menu";
            Stage stageAdminMenu = setupStage(title);
            VBox root = createVBox(30, 20, Pos.CENTER);

            Label lblTitle = createTittleLabel(title, 24);
            Label lblError = createErrorLabel();

            Button btnNewProduct = createMenuButton("Add product", e -> tryAction(lblError, this::createNewProduct, ""), 180);
            Button btnSetProduct = createMenuButton("Modificar productos", e -> {}, 180);
            Button btnDeleteProduct = createMenuButton("Eliminar productos", e -> {}, 180);
            Button btnSetUser = createMenuButton("Editar usuario", e -> {}, 180);
            Button btnDeleteUser = createMenuButton("Eliminar usuario", e -> {}, 180);
            Button btnReturnToMainMenu = createMenuButton("Regresar al menu principal", e -> returnToMainMenu(primaryStage, stageAdminMenu));

            HBox buttonContainer1 = new HBox(10);
            buttonContainer1.setAlignment(Pos.CENTER);
            buttonContainer1.getChildren().addAll(btnNewProduct, btnSetProduct);

            HBox buttonContainer2 = new HBox(10);
            buttonContainer2.setAlignment(Pos.CENTER);
            buttonContainer2.getChildren().addAll(btnDeleteProduct, btnSetUser);

            root.getChildren().addAll(lblTitle, buttonContainer1,buttonContainer2, btnDeleteUser, btnReturnToMainMenu, lblError);
            setScene(stageAdminMenu, root, 500, 600, "/styles/manuMainStyles.css");
            stageAdminMenu.showAndWait();
            
        }
    }
    
    private void createNewProduct() {
        String title = "New product";
        Stage stageNewProduct = setupStage(title);
        GridPane newProductGrid = createNewProductsGrid(stageNewProduct, title);
        VBox root = createVBox(15, 20, Pos.CENTER, newProductGrid);
        setScene(stageNewProduct, root, 450, 600, "/styles/manuMainStyles.css");

        stageNewProduct.showAndWait();
    }

    private GridPane createNewProductsGrid(Stage newProductStage, String title) {
        GridPane grid = createGridPane();
        addTitleToGrid(grid, title);

        List<InputField> fields = List.of(
             new InputField("Name: "),
             new InputField("Ref: "),
             new InputField("Cost: "),
             new InputField("Price: "),
             new InputField("Stock: "),
             new InputField("Bill: ")
        );

        Label lblCategory = new Label("Category: ");
        lblCategory.setStyle("-fx-font-size: 20px;");
        ComboBox<String> cbCategory = new ComboBox<>();
        cbCategory.getItems().addAll(Arrays.stream(Category.Categories.values())
                .map(category -> {
                    String name = category.name().toLowerCase();
                    if (!name.isEmpty()) {
                        return name.substring(0, 1).toUpperCase() + name.substring(1);
                    } else {
                        return name;
                    }
                })
                .toList());
        cbCategory.setPromptText("Select a category");

        Label lblSubcategory = new Label("Subcategory: ");
        lblSubcategory.setStyle("-fx-font-size: 20px;");
        ComboBox<String> cbSubCategory = new ComboBox<>();

        Label lblError = createErrorLabel();

        cbCategory.setOnAction(event -> {
            try {
                String selectedCategory = cbCategory.getSelectionModel().getSelectedItem().toUpperCase();
                cbSubCategory.getItems().clear();
                cbSubCategory.getItems().addAll(Arrays.stream(SubCategory.SubCategories.valueOf(selectedCategory).getItems())
                        .map(item -> {
                            String name = item.replace("_", " ");

                            String lowerCaseName = name.toLowerCase();
                            if (!lowerCaseName.isEmpty()) {
                                return lowerCaseName.substring(0, 1).toUpperCase() + lowerCaseName.substring(1);
                            } else {
                                return lowerCaseName;
                            }
                        })
                        .toList());
                cbSubCategory.setPromptText("Select a subcategory");
                showSuccess(lblError, "");
            } catch (Exception e) {
                showError(lblError, "Don´t found subcategory for selected category.");
            }
        });

        Button btnCreate = new Button("Create");
        btnCreate.setOnAction(event -> {

            String name = fields.get(0).getText().toLowerCase();
            String ref = fields.get(1).getText().toLowerCase();
            String costText = fields.get(2).getText();
            String priceText = fields.get(3).getText();
            String stockText = fields.get(4).getText();
            String bill = fields.get(5).getText().toLowerCase();
            String category = cbCategory.getSelectionModel().getSelectedItem();
            String subCategory = cbSubCategory.getSelectionModel().getSelectedItem();
            subCategory = subCategory.replace(" ", "_");

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

            fields.forEach(field -> field.setDisable(true));
            List<Node> controlsToDisable = new ArrayList<>(List.of(lblCategory, cbCategory, lblSubcategory, cbSubCategory, btnCreate, lblError));
            disableControls(controlsToDisable.toArray(new Node[0]));

            showSuccess(lblError, name + " product successfully added!");
            boolean continueCreate = askConfirmation("Create another product");
            closeAfterDelay(newProductStage);
            if (continueCreate) {
                createNewProduct();
            }
        });

        int row = 2;
        for (InputField field : fields) {
            grid.add(field.getLabel(), 0, row);
            grid.add(field.getTextField(), 1, row);
            row++;
        }

        grid.add(lblCategory, 0, row);
        grid.add(cbCategory, 1, row);
        row++;
        grid.add(lblSubcategory, 0, row);
        grid.add(cbSubCategory, 1, row);
        row++;
        grid.add(btnCreate, 0, row, 2, 1);
        grid.add(lblError, 0, row + 1, 2, 1);

        GridPane.setHalignment(btnCreate, HPos.CENTER);
        GridPane.setHalignment(lblError, HPos.CENTER);

        return grid;
    }
}
