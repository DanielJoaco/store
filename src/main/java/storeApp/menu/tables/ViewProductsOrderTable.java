package storeApp.menu.tables;

import storeApp.menu.shippingCar.ProductCardController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import storeApp.orders.Order;

import java.util.List;

public class ViewProductsOrderTable extends BaseTableController<Order.OrderItem> {

    private final List<Order.OrderItem> listOrderItems;

    public ViewProductsOrderTable(List<Order.OrderItem> listOrderItems) {
        this.listOrderItems = listOrderItems;
    }

    @Override
    protected void setupColumns() {
        // Crear columnas con títulos y ancho
        TableColumn<Order.OrderItem, String> colReference = new TableColumn<>("Reference");
        colReference.setPrefWidth(80);

        TableColumn<Order.OrderItem, Integer> colQuantity = new TableColumn<>("Quantity");
        colQuantity.setPrefWidth(80);

        TableColumn<Order.OrderItem, String> colName = new TableColumn<>("Name");
        colName.setPrefWidth(200);

        TableColumn<Order.OrderItem, Double> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setPrefWidth(120);

        TableColumn<Order.OrderItem, Double> colTotal = new TableColumn<>("Total");
        colTotal.setPrefWidth(120);

        // Configurar cell value factories directamente con lambdas para acceder a las propiedades anidadas
        colReference.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().productInfo().getRef()));

        colQuantity.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().quantity()).asObject());

        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().productInfo().getName()));

        colSubtotal.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().unitPrice()).asObject());

        colTotal.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().unitPrice()*cellData.getValue().quantity()).asObject());

        tableView.getColumns().addAll(
                colReference, colQuantity, colName, colSubtotal, colTotal
        );
    }

    @Override
    public void loadData() {
        dataList.clear();
        dataList.addAll(listOrderItems);
    }
}