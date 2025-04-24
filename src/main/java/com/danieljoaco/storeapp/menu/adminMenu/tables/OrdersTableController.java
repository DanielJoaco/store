package com.danieljoaco.storeapp.menu.adminMenu.tables;

import javafx.scene.control.TableColumn;

/**
 * Controller for the orders table
 * This is a placeholder that can be expanded when order functionality is implemented
 */
public class OrdersTableController extends BaseTableController<Object> {

    @Override
    protected void setupColumns() {
        // Add basic columns that can be expanded later
        TableColumn<Object, String> colOrderId = createColumn("Order ID", "id", 100);
        TableColumn<Object, String> colOrderDate = createColumn("Date", "date", 120);
        TableColumn<Object, String> colOrderCustomer = createColumn("Customer", "customer", 200);

        tableView.getColumns().addAll(
                colOrderId, colOrderDate, colOrderCustomer
        );
    }

    @Override
    public void loadData() {
        // This will be implemented when order functionality is added
        System.out.println("Order data loading not yet implemented");
    }
}