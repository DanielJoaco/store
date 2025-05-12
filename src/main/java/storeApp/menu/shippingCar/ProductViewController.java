package storeApp.menu.shippingCar;

import storeApp.db.ProductsDao;
import storeApp.menu.forms.OrderFormController;
import storeApp.orders.Order;
import storeApp.product.SubCategory;
import storeApp.user.Customer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static storeApp.menu.utils.Utils.capitalize;

public class ProductViewController {

    @FXML
    private ScrollPane gridProductPane;

    @FXML
    private VBox boxCategories;

    @FXML
    private HBox boxExplorationButtons;

    @FXML
    private Label cartCountLabel;

    @FXML
    private Button newOrderButton;

    private List<ProductsDao.ProductViewInfo> products;
    private Customer.CustomerInfo customer;
    private Stage productViewStage;
    private List<GridPane> productPages;
    private List<ProductCardController> productCardControllers;
    private int cartItemCount;
    private int currentPageIndex;

    public void initialize(Customer.CustomerInfo customer, Stage stage) {
        this.products = loadProducts();
        this.customer = customer;
        this.productViewStage = stage;
        this.productPages = new ArrayList<>();
        this.productCardControllers = new ArrayList<>();
        this.cartItemCount = 0;
        this.currentPageIndex = 0;

        // Initialize cart count label if present
        if (cartCountLabel != null) {
            updateCartCountLabel();
        }

        setupCategoriesTittlePane();
        setupViewProductsGrid();
        createExplorationButtons();

        // Initially disable New Order button if cart is empty
        if (newOrderButton != null) {
            newOrderButton.setDisable(cartItemCount == 0);
        }
    }

    private void updateCartCountLabel() {
        cartItemCount = 0;
        for (ProductCardController controller : productCardControllers) {
            if (controller.getOrderItem() != null) {
                cartItemCount++;
            }
        }

        if (cartCountLabel != null) {
            cartCountLabel.setText("Cart: " + cartItemCount + " items");
        }

        // Enable/disable New Order button based on cart contents
        if (newOrderButton != null) {
            newOrderButton.setDisable(cartItemCount == 0);
        }
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
            SubCategory.SubCategories subCategoryEnum = SubCategory.SubCategories.valueOf(category);

            List<Hyperlink> subcategoryLinks = new ArrayList<>();
            for (String sub : subCategoryEnum.getItems()) {
                Hyperlink link = new Hyperlink(capitalize(sub));
                String finalSub = sub;
                link.setOnAction(event -> filterBySubcategory(finalSub));
                subcategoryLinks.add(link);
            }

            VBox vbox = new VBox(5);
            vbox.getChildren().addAll(subcategoryLinks);

            TitledPane titledPane = new TitledPane(capitalize(category), vbox);
            titledPane.setExpanded(false);
            boxCategories.getChildren().add(titledPane);
        }
    }

    private void filterBySubcategory(String subcategory) {
        // Implement filtering logic here
        System.out.println("Filtering by subcategory: " + subcategory);
        // You would typically filter your products list and reload the grid
    }

    private void setupViewProductsGrid() {
        int row = 0, col = 0, count = 0, pageIdx = 0;
        GridPane currentPage = createGridPane();

        for (ProductsDao.ProductViewInfo product : products) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product-card.fxml"));
            Node card;
            try {
                card = loader.load();

            }catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading product card: " + e.getMessage());
                continue; // Skip this product if loading fails
            }
            ProductCardController ctrl = loader.getController();
            ctrl.initData(product);
            // Listener del botón…
            Button btn = findAddButton(card);
            if (btn != null) {
                btn.setOnAction(e -> { ctrl.addToCart(); updateCartCountLabel(); });
            }
            productCardControllers.add(ctrl);

            // Añade la tarjeta al grid actual
            currentPage.add(card, col, row);
            count++; col = (col + 1) % 3;
            if (col == 0) row++;

            // Cuando llegues a 6 productos, inicia nueva página
            if (count == 6) {
                productPages.add(currentPage);
                currentPage = createGridPane();
                count = row = col = 0;
                pageIdx++;
            }
        }
        // Añade la última página (si quedó con <6 items)
        if (count > 0 || productPages.isEmpty()) {
            productPages.add(currentPage);
        }

        // Muestra la primera página
        gridProductPane.setContent(productPages.get(0));
    }

    private Button findAddButton(Node cardNode) {
        return (Button) ((VBox) cardNode).getChildren().stream()
                .filter(n -> n instanceof Button && ((Button) n).getText().equals("Add to Cart"))
                .findFirst().orElse(null);
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

    private void createExplorationButtons() {
        boxExplorationButtons.getChildren().clear();

        // Declara e inicializa aquí tu lista local
        List<Button> explorationButtons = new ArrayList<>();

        int totalPages = productPages.size();
        for (int i = 0; i < totalPages; i++) {
            Button btn = new Button(String.valueOf(i + 1));
            btn.setId(String.valueOf(i));
            btn.setOnAction(e -> changePage(btn.getId()));
            explorationButtons.add(btn);
        }

        boxExplorationButtons.getChildren().addAll(explorationButtons);
    }



    private void changePage(String id) {
        int idx = Integer.parseInt(id);
        if (idx >= 0 && idx < productPages.size()) {
            currentPageIndex = idx;
            gridProductPane.setContent(productPages.get(idx));
        }
    }


    @FXML
    private void prevPage() {
        if (currentPageIndex > 0) {
            changePage(String.valueOf(currentPageIndex - 1));
        }
    }

    @FXML
    private void nextPage() {
        if (currentPageIndex < productPages.size() - 1) {
            changePage(String.valueOf(currentPageIndex + 1));
        }
    }


    @FXML
    private void newOrder() {
        try {
            List<Order.OrderItem> listOrderItems = new ArrayList<>();

            for (ProductCardController controller : productCardControllers) {
                Order.OrderItem orderItem = controller.getOrderItem();
                if (orderItem != null) {
                    listOrderItems.add(orderItem);
                }
            }

            if (listOrderItems.isEmpty()) {
                // Show an alert if no products were selected
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Empty Cart");
                alert.setHeaderText(null);
                alert.setContentText("Please add at least one product to your cart.");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order-form.fxml"));
            Parent root;
            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading order form: " + e.getMessage());
                return;
            }

            Scene scene = new Scene(root);
            Stage orderFormStage = new Stage();
            orderFormStage.setTitle("Order Form");
            orderFormStage.setScene(scene);

            // Get the controller after loading the FXML
            OrderFormController orderFormController = loader.getController();
            orderFormController.initialize(listOrderItems, customer, orderFormStage);

            productViewStage.close();
            orderFormStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading order form: " + e.getMessage());
        }
    }

    @FXML
    private void clearCart() {
        for (ProductCardController controller : productCardControllers) {
            controller.clearProductCardInfo();
        }
        updateCartCountLabel();

        // Show confirmation alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cart Cleared");
        alert.setHeaderText(null);
        alert.setContentText("Your shopping cart has been cleared.");
        alert.showAndWait();
    }
}