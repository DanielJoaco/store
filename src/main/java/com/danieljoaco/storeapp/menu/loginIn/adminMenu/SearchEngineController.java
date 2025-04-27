package com.danieljoaco.storeapp.menu.loginIn.adminMenu;

import com.danieljoaco.storeapp.products.ProductReference;
import com.danieljoaco.storeapp.users.UserDao;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import static com.danieljoaco.storeapp.db.ProductsDao.searchProductReferences;
import static com.danieljoaco.storeapp.menu.utils.Utils.closeAfterDelay;

public class SearchEngineController {

    @FXML
    private Label lblTitle, lblSearch, lblError;

    @FXML
    private TextField tfSearch;

    @FXML
    private Button btnSearch;

    private Stage searchEngineStage;
    private ProductReference selectedProduct;
    private UserDao.BasicUserInfoDb selectedUser;


    public enum SearchType {
        PRODUCT("Search Product", "Ref, name \nor brand: "),
        USER ("Search User", "Id: "),
        ORDER ("Search Order", "N°: ");

        private final String tittle;
        private final String searchText;
        SearchType(String tittle, String searchText) {
            this.tittle = tittle;
            this.searchText = searchText;
        }
        public String getTittle() {
            return tittle;
        }
        public String getSearchText() {
            return searchText;
        }
    }

    public void initialize(Stage searchEngineStage, String tittle, String searchText) {
        this.lblTitle.setText(tittle);
        this.lblSearch.setText(searchText);
        this.searchEngineStage = searchEngineStage;
        setupSearch(tittle);
    }

    private void setupSearch(String tittle) {
        btnSearch.setOnAction(actionEvent -> {
            String searchText = tfSearch.getText();
            switch (tittle) {
                case "Search Product": {
                    ObservableList<ProductReference> productsList = searchProductReferences(searchText);
                    if (productsList.isEmpty()) {
                        lblError.setText("No products were found for:" + searchText);
                    } else if (productsList.size() == 1) {
                        selectedProduct = productsList.getFirst();
                        lblError.setText("Product found");
                        closeAfterDelay(searchEngineStage);
                    } else {
                        showProductSelectionDialog(productsList);
                    }
                    break;
                }
                case "Search User":{
                    ObservableList<UserDao.BasicUserInfoDb> usersList = UserDao.findUser(searchText);
                    if(usersList.isEmpty()) {
                        lblError.setText("No User found for:" + searchText);
                    } else if (usersList.size() == 1) {
                        selectedUser = usersList.getFirst();
                        lblError.setText("User found");
                        closeAfterDelay(searchEngineStage);
                    }else{
                        showUserSelectionDialog(usersList);
                    }
                }
            }
        });
    }

    private void showProductSelectionDialog(ObservableList<ProductReference> products) {
        ListView<ProductReference> listView = getProductReferenceListView(products);

        Dialog<ProductReference> dialog = new Dialog<>();
        dialog.setTitle("Select product");
        dialog.setHeaderText("Multiple products found. Please select one:");

        ButtonType seleccionarButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(seleccionarButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == seleccionarButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            selectedProduct = product;
            lblError.setText("Selected product");
            closeAfterDelay(searchEngineStage);
        });
    }

    private ListView<ProductReference> getProductReferenceListView(ObservableList<ProductReference> products) {
        ListView<ProductReference> listView = new ListView<>(products);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductReference product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName() + " (Ref: " + product.getRef() + ")");
                }
            }
        });
        return listView;
    }

    private void showUserSelectionDialog(ObservableList<UserDao.BasicUserInfoDb> users) {
        ListView<UserDao.BasicUserInfoDb> listView = getUserReferenceListView(users);

        Dialog<UserDao.BasicUserInfoDb> dialog = new Dialog<>();
        dialog.setTitle("Select user");
        dialog.setHeaderText("Multiple users found. Please select one:");

        ButtonType seleccionarButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(seleccionarButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == seleccionarButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            selectedUser = user;
            lblError.setText("Selected user");
            closeAfterDelay(searchEngineStage);
        });
    }

    private ListView<UserDao.BasicUserInfoDb> getUserReferenceListView(ObservableList<UserDao.BasicUserInfoDb> users) {
        ListView<UserDao.BasicUserInfoDb> listView = new ListView<>(users);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserDao.BasicUserInfoDb user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.name() + " (Id: " + user.id() + ")");
                }
            }
        });
        return listView;
    }

    public ProductReference getSelectedProduct() {
        return selectedProduct;
    }
}
