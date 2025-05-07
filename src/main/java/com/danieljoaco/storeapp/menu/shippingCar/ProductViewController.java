package com.danieljoaco.storeapp.menu.shippingCar;

import com.danieljoaco.storeapp.db.ProductsDao;
import com.danieljoaco.storeapp.product.SubCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.danieljoaco.storeapp.menu.shippingCar.ProductCardController.getProductCardInfoList;
import static com.danieljoaco.storeapp.menu.utils.Utils.capitalize;

public class ProductViewController {

    @FXML
    private ScrollPane gridProductPane;

    @FXML
    private VBox boxCategories;

    @FXML
    private HBox boxExplorationButtons;

    private List<ProductsDao.ProductViewInfo> products;

    public void initialize() {
        this.products = loadProducts();
        setupCategoriesTittlePane();
        setupViewProductsGrid();
        createExplorationButtons();
    }

    private List<ProductsDao.ProductViewInfo> loadProducts() {
        return ProductsDao.getAllProductsView();
    }

    private void setupCategoriesTittlePane() {
        boxCategories.getChildren().clear();
        Set<String> uniqueCategories = products.stream()
                .map(ProductsDao.ProductViewInfo::category)
                .filter(c -> SubCategory.SubCategories.fromCategory(c).isPresent())
                .collect(Collectors.toSet());

        for (String category : uniqueCategories) {
            System.out.println(category);
            SubCategory.SubCategories subCategoryEnum = SubCategory.SubCategories.valueOf(category);

            List<Hyperlink> subcategoryLinks = new ArrayList<>();
            for (String sub : subCategoryEnum.getItems()) {
                Hyperlink link = new Hyperlink(capitalize(sub));
                subcategoryLinks.add(link);
            }

            VBox vbox = new VBox(5);
            vbox.getChildren().addAll(subcategoryLinks);

            TitledPane titledPane = new TitledPane(capitalize(category), vbox);
            titledPane.setExpanded(false);
            boxCategories.getChildren().add(titledPane);
        }
    }



    private void setupViewProductsGrid() {
        int row = 0;
        int col = 0;
        int numberProducts = 0;
        int numerGridPane = 0;

        GridPane gridProductsPage = createGridPane();
        gridProductsPage.setId(String.valueOf(numerGridPane));

        BorderPane centeringPane = new BorderPane();
        centeringPane.setCenter(gridProductsPage);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(centeringPane);


        for (ProductsDao.ProductViewInfo product : products) {
            FXMLLoader cardLoader = new FXMLLoader(getClass().getResource("/fxml/product-card.fxml"));
            Node cardNode;
            try {
                cardNode = cardLoader.load();
            }catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading product card: " + e.getMessage());
                continue;
            }
            ProductCardController cardCtrl = cardLoader.getController();
            cardCtrl.initData(product);

            if (numberProducts < 6) {
                gridProductsPage.add(cardNode, col, row);
                numberProducts++;
            }else{
                numberProducts = 0;
                col = 0;
                row = 0;
                numerGridPane++;

                gridProductsPage = createGridPane();
                gridProductsPage.setId(String.valueOf(numerGridPane));
                gridProductsPage.setVisible(false);
                gridProductsPage.add(cardNode, col, row);

                centeringPane = new BorderPane();
                centeringPane.setCenter(gridProductsPage);
                stackPane.getChildren().add(centeringPane);
            }

            col++;
            if (col > 2) {
                col = 0;
                row++;
            }

        }

        gridProductPane.setContent(stackPane);
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.getColumnConstraints().clear();

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setPercentWidth(33.33);
            gridPane.getColumnConstraints().add(column);
        }

        for (int i = 0; i < 2; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(50);
            row.setMinHeight(220);
            row.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(row);
        }

        return gridPane;
    }

    private void createExplorationButtons(){
        int items = 1;
        int page = 0;
        List<Button> explorationButtons = new ArrayList<>();
        Button button = new Button("1");
        button.setId(String.valueOf(page));
        Button finalButton = button;
        button.setOnAction(event -> {changePage(finalButton.getId());});
        explorationButtons.add(button);
        for(ProductsDao.ProductViewInfo p: products){
            items++;
            if(items > 6){
                page++;
                button = new Button(String.valueOf(page+1));
                button.setId(String.valueOf(page));
                Button finalButton2 = button;
                button.setOnAction(event -> {changePage(finalButton2.getId());});
                explorationButtons.add(button);
                items = 1;

            }
        }

        boxExplorationButtons.getChildren().clear();
        boxExplorationButtons.getChildren().addAll(explorationButtons);
    }

    private void changePage(String id) {
        Node content = gridProductPane.getContent();
        if (content instanceof StackPane stackPane) {
            for (Node innerNode : stackPane.getChildren()) {
                if (innerNode instanceof BorderPane borderPane) {
                    Node center = borderPane.getCenter();
                    if (center instanceof GridPane gridProductsPage) {
                        gridProductsPage.setVisible(gridProductsPage.getId().equals(id));
                    }
                }
            }
        }
    }

    @FXML
    private void prevPage() {
        Node content = gridProductPane.getContent();
        if (content instanceof StackPane stackPane) {
            for (Node innerNode : stackPane.getChildren()) {
                if (innerNode instanceof BorderPane borderPane) {
                    Node center = borderPane.getCenter();
                    if (center instanceof GridPane gridProductsPage) {
                        int currentPage = Integer.parseInt(gridProductsPage.getId());
                        if (currentPage > 0) {
                            changePage(String.valueOf(currentPage - 1));
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void nextPage() {
        Node content = gridProductPane.getContent();
        if (content instanceof StackPane stackPane) {
            for (Node innerNode : stackPane.getChildren()) {
                if (innerNode instanceof BorderPane borderPane) {
                    Node center = borderPane.getCenter();
                    if (center instanceof GridPane gridProductsPage) {
                        int currentPage = Integer.parseInt(gridProductsPage.getId());
                        changePage(String.valueOf(currentPage));
                    }
                }
            }
        }
    }

    @FXML
    private void newOrder(){
        List<ProductCardController.ProductCardInfo> productCardInfoList = getProductCardInfoList();

    }
}
