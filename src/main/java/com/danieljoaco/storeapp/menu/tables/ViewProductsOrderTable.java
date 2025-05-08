package com.danieljoaco.storeapp.menu.tables;

import com.danieljoaco.storeapp.menu.shippingCar.ProductCardController;
import javafx.scene.control.TableColumn;

import java.util.List;

public class ViewProductsOrderTable extends BaseTableController<ProductCardController.ProductCardInfo> {

        private List<ProductCardController.ProductCardInfo> productCardInfoList;

        public void initialize(List<ProductCardController.ProductCardInfo> productCardInfoList) {
            this.productCardInfoList = productCardInfoList;
            setupColumns();
            loadData();

        }

        @Override
        protected void setupColumns() {
            TableColumn<ProductCardController.ProductCardInfo, String> colReference = createColumn("Reference", "productViewInfo.ref", 80);
            TableColumn<ProductCardController.ProductCardInfo, Integer> colQuantity = createColumn("Quantity", "quantity", 80);
            TableColumn<ProductCardController.ProductCardInfo, String> colName = createColumn("Name", "productViewInfo.name", 200);
            TableColumn<ProductCardController.ProductCardInfo, Double> colSubtotal = createColumn("Subtotal", "productViewInfo.total", 120);
            TableColumn<ProductCardController.ProductCardInfo, Double> colTotal = createColumn("Total", "total", 120);

            tableView.getColumns().addAll(
                    colReference, colQuantity, colName, colSubtotal, colTotal
            );
        }



        @Override
        public void loadData() {}
}

