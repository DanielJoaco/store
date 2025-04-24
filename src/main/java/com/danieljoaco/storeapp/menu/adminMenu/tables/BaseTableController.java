package com.danieljoaco.storeapp.menu.adminMenu.tables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

/**
 * Abstract base class for all table controllers
 * @param <T> Type of data to be displayed in the table
 */
public abstract class BaseTableController<T> {

    protected TableView<T> tableView;
    protected final ObservableList<T> dataList = FXCollections.observableArrayList();

    /**
     * Creates and configures the table with its columns
     * @return The configured TableView
     */
    public TableView<T> createTable() {
        tableView = new TableView<>();
        tableView.setPrefSize(840, 605);

        setupColumns();

        tableView.setItems(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        return tableView;
    }

    /**
     * Adds the table to the specified container
     * @param container The container to add the table to
     */
    public void addTableToContainer(AnchorPane container) {
        AnchorPane.setTopAnchor(tableView, 0.0);
        AnchorPane.setRightAnchor(tableView, 0.0);
        AnchorPane.setBottomAnchor(tableView, 0.0);
        AnchorPane.setLeftAnchor(tableView, 0.0);
        container.getChildren().add(tableView);
    }

    /**
     * Creates a column with the specified title, property name and width
     * @param title The title of the column
     * @param propertyName The property name to bind to
     * @param width The preferred width of the column
     * @return The created TableColumn
     */
    protected <S> TableColumn<T, S> createColumn(String title, String propertyName, double width) {
        TableColumn<T, S> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        return column;
    }

    /**
     * Loads data into the table
     */
    public abstract void loadData();

    /**
     * Sets up the columns for the table
     */
    protected abstract void setupColumns();

    /**
     * Gets the TableView instance
     * @return The TableView
     */
    public TableView<T> getTableView() {
        return tableView;
    }

    /**
     * Gets the ObservableList containing the table data
     * @return The ObservableList of data
     */
    public ObservableList<T> getDataList() {
        return dataList;
    }

    /**
     * Helper method to capitalize the first letter of a string
     * @param str The string to capitalize
     * @return The capitalized string
     */
    protected String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}