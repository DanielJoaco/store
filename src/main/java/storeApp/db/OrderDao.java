package storeApp.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import storeApp.orders.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storeApp.product.ProductInfo;
import storeApp.user.Customer;

import static storeApp.db.ProductsDao.searchProductByQuery;
import static storeApp.orders.OrderService.createOrder;
import static storeApp.user.UserDao.findUserById;

public class OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    // SQL Queries como constantes
    private static final String INSERT_ORDER_SQL = """
        INSERT INTO orders (id, user_id, order_date, status_id, payment_id, franchise_id,
        subtotal, shipping_cost, tax, discount, total, shipping_id, tracking_number, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_ORDER_SQL = """
        UPDATE orders
        SET order_date = ?, status_id = ?, payment_id = ?, franchise_id = ?, subtotal = ?,
        shipping_cost = ?, tax = ?, discount = ?, total = ?,
        tracking_number = ?, notes = ?
        WHERE id = ?
        """;

    private static final String SELECT_ALL_ORDERS_SQL = "SELECT * FROM orders";
    private static final String SELECT_ORDER_ITEMS_SQL = "SELECT * FROM order_items WHERE order_id = ?";
    private static final String SELECT_STATUS_HISTORY_SQL = "SELECT * FROM status_history WHERE order_id = ?";
    private static final String SELECT_SHIPPING_ADDRESS_SQL = "SELECT * FROM shipping_address WHERE id = ?";
    private static final String SELECT_ORDER_STATUS_SQL = "SELECT * FROM order_statuses WHERE id = ?";

    private static final String INSERT_SHIPPING_ADDRESS_SQL = """
        INSERT INTO shipping_address (street_id, st_number, st_letter, cross_st_number,
        cross_st_letter, house_number, house_letter, indications, city, postal_code, state, country)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SHIPPING_ADDRESS_SQL = """
        UPDATE shipping_address
        SET street_id = ?, st_number = ?, st_letter = ?, cross_st_number = ?,
        cross_st_letter = ?, house_number = ?, house_letter = ?, indications = ?,
        city = ?, postal_code = ?, state = ?, country = ?
        WHERE id = ?
        """;

    public static boolean createOrderInDb(Order order) {
        return executeOrderTransaction(order, OrderDao::performCreateOrder);
    }

    public static boolean editOrder(Order order) {
        return executeOrderTransaction(order, OrderDao::performEditOrder);
    }

    public static ObservableList<Order> getAllOrders() {
        return executeTransactionWithResult(OrderDao::performGetAllOrders, FXCollections.observableArrayList());
    }

    // Método genérico para ejecutar transacciones
    private static boolean executeOrderTransaction(Order order, OrderTransactionFunction function) {
        return executeTransactionWithResult(conn -> {
            function.execute(conn, order);
            return true;
        }, false);
    }

    // Método genérico para transacciones con resultado
    private static <T> T executeTransactionWithResult(TransactionFunction<T> function, T defaultValue) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = function.execute(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            logger.error("Error executing database transaction", e);
            rollbackConnection(conn);
            return defaultValue;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    // Lógica específica para crear orden
    private static void performCreateOrder(Connection conn, Order order) throws SQLException {
        OrderContext context = prepareOrderContext(conn, order);

        int shippingAddressId = insertShippingAddress(conn, order.getShippingAddress(), context.streetId);
        if (shippingAddressId == -1) {
            throw new SQLException("Failed to insert shipping address");
        }

        insertOrder(conn, order, context, shippingAddressId);
        insertOrderItems(conn, order.getId(), order.getItems());
        insertStatusHistory(conn, order.getId(), context.statusId, order.getLastStatus().dateTime());
    }

    // Lógica específica para editar orden
    private static void performEditOrder(Connection conn, Order order) throws SQLException {
        OrderContext context = prepareOrderContext(conn, order);

        int shippingId = getShippingIdForOrder(conn, order.getId());
        if (shippingId == -1) {
            throw new SQLException("Order shipping address not found");
        }

        updateShippingAddress(conn, shippingId, context.streetId, order.getShippingAddress());
        updateOrder(conn, order, context);

        deleteOrderItems(conn, order.getId());
        insertOrderItems(conn, order.getId(), order.getItems());
        insertStatusHistory(conn, order.getId(), context.statusId, order.getLastStatus().dateTime());
    }

    // Clase para encapsular el contexto de la orden
    private static class OrderContext {
        final int paymentMethodId;
        final int franchiseId;
        final int streetId;
        final int userId;
        final int statusId;

        OrderContext(int paymentMethodId, int franchiseId, int streetId, int userId, int statusId) {
            this.paymentMethodId = paymentMethodId;
            this.franchiseId = franchiseId;
            this.streetId = streetId;
            this.userId = userId;
            this.statusId = statusId;
        }
    }

    // Método para preparar el contexto común de las operaciones
    private static OrderContext prepareOrderContext(Connection conn, Order order) throws SQLException {
        int paymentMethodId = getIdByName(conn, "payment_data", "methods_name",
                order.getPaymentData().paymentMethod().name());
        if (paymentMethodId == -1) {
            throw new SQLException("Failed to get payment method ID");
        }

        int franchiseId = order.getPaymentData().franchises() == null ? 0 :
                getIdByName(conn, "franchises", "name", order.getPaymentData().franchises().name());

        int streetId = getIdByName(conn, "streets", "name", order.getShippingAddress().st().name());
        if (streetId == -1) {
            throw new SQLException("Failed to get street ID");
        }

        int userId = getIdByName(conn, "users", "email", order.getCustomerInfo().email());
        if (userId == -1) {
            throw new SQLException("User not found");
        }

        int statusId = getIdByName(conn, "order_statuses", "name", order.getLastStatus().status().name());
        if (statusId == -1) {
            throw new SQLException("Invalid order status");
        }

        return new OrderContext(paymentMethodId, franchiseId, streetId, userId, statusId);
    }

    // Método genérico para obtener IDs por nombre
    private static int getIdByName(Connection conn, String tableName, String columnName, String value) throws SQLException {
        String sql = String.format("SELECT id FROM %s WHERE %s = ?", tableName, columnName);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    // Insertar orden usando el contexto
    private static void insertOrder(Connection conn, Order order, OrderContext context, int shippingAddressId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_ORDER_SQL)) {
            setOrderParameters(pstmt, order, context, shippingAddressId, false);
            pstmt.executeUpdate();
        }
    }

    // Actualizar orden usando el contexto
    private static void updateOrder(Connection conn, Order order, OrderContext context) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_ORDER_SQL)) {
            setOrderParameters(pstmt, order, context, -1, true);
            pstmt.executeUpdate();
        }
    }

    // Método común para establecer parámetros de orden
    private static void setOrderParameters(PreparedStatement pstmt, Order order, OrderContext context,
                                           int shippingAddressId, boolean isUpdate) throws SQLException {
        int paramIndex = 1;

        if (!isUpdate) {
            pstmt.setString(paramIndex++, order.getId());
            pstmt.setInt(paramIndex++, context.userId);
        }

        pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(order.getOrderDate()));
        pstmt.setInt(paramIndex++, context.statusId);
        pstmt.setInt(paramIndex++, context.paymentMethodId);
        pstmt.setInt(paramIndex++, context.franchiseId);
        pstmt.setDouble(paramIndex++, order.getSubtotal());
        pstmt.setDouble(paramIndex++, order.getShippingCost());
        pstmt.setDouble(paramIndex++, order.getTax());
        pstmt.setDouble(paramIndex++, order.getDiscount());
        pstmt.setDouble(paramIndex++, order.getTotal());

        if (!isUpdate) {
            pstmt.setInt(paramIndex++, shippingAddressId);
        }

        pstmt.setString(paramIndex++, order.getTrackingNumber());
        pstmt.setString(paramIndex++, order.getNotes());

        if (isUpdate) {
            pstmt.setString(paramIndex, order.getId());
        }
    }

    // Lógica para obtener todas las órdenes
    private static ObservableList<Order> performGetAllOrders(Connection conn) throws SQLException {
        ObservableList<Order> orders = FXCollections.observableArrayList();

        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_ORDERS_SQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Order order = buildOrderFromResultSet(conn, rs);
                orders.add(order);
            }
        }

        return orders;
    }

    // Construir orden desde ResultSet
    private static Order buildOrderFromResultSet(Connection conn, ResultSet rs) throws SQLException {
        String orderId = rs.getString("id");
        int userId = rs.getInt("user_id");
        LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();
        int statusId = rs.getInt("status_id");
        int paymentMethodId = rs.getInt("payment_id");
        int franchiseId = rs.getInt("franchise_id");
        double shippingCost = rs.getDouble("shipping_cost");
        double tax = rs.getDouble("tax");
        double discount = rs.getDouble("discount");
        int shippingAddressId = rs.getInt("shipping_id");
        String trackingNumber = rs.getString("tracking_number");
        String notes = rs.getString("notes");

        Customer.CustomerInfo customerInfo = getCustomerInfo(userId);
        List<Order.OrderItem> items = getOrderItems(conn, orderId);
        List<Order.StatusHistory> statusHistory = getStatusHistory(conn, orderId);
        Payment.PaymentData paymentData = getPaymentData(conn, paymentMethodId, franchiseId);
        Address shippingAddress = getShippingAddress(conn, shippingAddressId);
        Order.Status status = getOrderStatus(conn, statusId);

        return createOrder(orderId, customerInfo, items, orderDate, status, statusHistory,
                paymentData, shippingCost, tax, discount, shippingAddress, trackingNumber, notes);
    }

    // Métodos auxiliares para construir la orden
    private static Customer.CustomerInfo getCustomerInfo(int userId) throws SQLException {
        try {
            Customer customer = (Customer) findUserById(userId);
            if (customer == null) {
                throw new IllegalArgumentException("Could not find customer");
            }
            return customer.getCustomerInfo();
        } catch (Exception e) {
            throw new SQLException("Could not find customer", e);
        }
    }

    private static List<Order.OrderItem> getOrderItems(Connection conn, String orderId) throws SQLException {
        List<Order.OrderItem> items = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_ORDER_ITEMS_SQL)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    ObservableList<ProductInfo> productsInfo = searchProductByQuery(productId);

                    Order.OrderItem orderItem = new Order.OrderItem(
                            productsInfo.getFirst(),
                            productId,
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    );
                    items.add(orderItem);
                }
            }
        }
        return items;
    }

    private static List<Order.StatusHistory> getStatusHistory(Connection conn, String orderId) throws SQLException {
        List<Order.StatusHistory> statusHistory = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_STATUS_HISTORY_SQL)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("status_id");
                    Order.Status nameStatus = Order.Status.values()[id];
                    LocalDateTime date = rs.getTimestamp("updated_at").toLocalDateTime();
                    Order.StatusHistory newStatus = new Order.StatusHistory(nameStatus, date);
                    statusHistory.add(newStatus);
                }
            }
        }
        return statusHistory;
    }

    private static Payment.PaymentData getPaymentData(Connection conn, int paymentMethodId, int franchiseId) throws SQLException {
        String methodName = null;
        String franchise = null;

        String sqlPaymentData = """
            SELECT 'payment_data' AS source, methods_name AS value
            FROM payment_data
            WHERE id = ?
            UNION ALL
            SELECT 'franchises' AS source, name AS value
            FROM franchises
            WHERE id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sqlPaymentData)) {
            pstmt.setInt(1, paymentMethodId);
            pstmt.setInt(2, franchiseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String source = rs.getString("source");
                    String value = rs.getString("value");

                    if ("payment_data".equals(source)) {
                        methodName = value;
                    } else if ("franchises".equals(source)) {
                        franchise = value;
                    }
                }
            }
        }

        return new Payment.PaymentData(
                Payment.PaymentMethod.valueOf(methodName),
                "null".equals(franchise) ? null : Payment.Franchises.valueOf(franchise)
        );
    }

    private static Address getShippingAddress(Connection conn, int shippingAddressId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_SHIPPING_ADDRESS_SQL)) {
            pstmt.setInt(1, shippingAddressId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Address(
                            StreetType.values()[rs.getInt("street_id")],
                            rs.getInt("st_number"),
                            rs.getString("st_letter"),
                            rs.getInt("cross_st_number"),
                            rs.getString("cross_st_letter"),
                            rs.getInt("house_number"),
                            rs.getString("house_letter"),
                            rs.getString("indications"),
                            rs.getString("postal_code"),
                            rs.getString("city"),
                            rs.getString("state"),
                            rs.getString("country")
                    );
                }
            }
        }
        return null;
    }

    private static Order.Status getOrderStatus(Connection conn, int statusId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_ORDER_STATUS_SQL)) {
            pstmt.setInt(1, statusId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Order.Status.valueOf(rs.getString("name"));
                } else {
                    throw new SQLException("Order status not found");
                }
            }
        }
    }

    // Actualización de stock de producto
    private static void updateProductStock(Connection conn, String productId, int quantity) throws SQLException {
        String sqlUpdateStock = "UPDATE products SET stock = stock - ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateStock)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productId);
            pstmt.executeUpdate();
        }
    }

    // Métodos simplificados para insertar/actualizar direcciones de envío
    private static int insertShippingAddress(Connection conn, Address address, int streetId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SHIPPING_ADDRESS_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setShippingAddressParameters(pstmt, address, streetId, false);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                return generatedKeys.next() ? generatedKeys.getInt(1) : -1;
            }
        }
    }

    private static void updateShippingAddress(Connection conn, int shippingId, int streetId, Address address) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_SHIPPING_ADDRESS_SQL)) {
            setShippingAddressParameters(pstmt, address, streetId, true);
            pstmt.setInt(13, shippingId);
            pstmt.executeUpdate();
        }
    }

    // Método común para establecer parámetros de dirección de envío
    private static void setShippingAddressParameters(PreparedStatement pstmt, Address address, int streetId, boolean isUpdate) throws SQLException {
        pstmt.setInt(1, streetId);
        pstmt.setInt(2, address.stNum());
        pstmt.setString(3, address.stLet());
        pstmt.setInt(4, address.crossStNum());
        pstmt.setString(5, address.crossStLet());
        pstmt.setInt(6, address.houseNum());
        pstmt.setString(7, address.houseLet());
        pstmt.setString(8, address.indications());
        pstmt.setString(9, address.city());
        pstmt.setString(10, address.postalCode());
        pstmt.setString(11, address.state());
        pstmt.setString(12, address.country());
    }

    // Métodos de inserción y eliminación de items de orden
    private static void insertOrderItems(Connection conn, String orderId, List<Order.OrderItem> items) throws SQLException {
        String sql = """
            INSERT INTO order_items (order_id, product_id, quantity, unit_price, item_discount)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Order.OrderItem item : items) {
                pstmt.setString(1, orderId);
                pstmt.setString(2, item.productId());
                pstmt.setInt(3, item.quantity());
                pstmt.setDouble(4, item.unitPrice());
                pstmt.setDouble(5, 0.0); // Default discount is 0
                pstmt.addBatch();
                updateProductStock(conn, item.productId(), item.quantity());
            }
            pstmt.executeBatch();
        }
    }

    private static void deleteOrderItems(Connection conn, String orderId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            pstmt.executeUpdate();
        }
    }

    private static void insertStatusHistory(Connection conn, String orderId, int statusId, LocalDateTime dateTime) throws SQLException {
        String sql = "INSERT INTO status_history (order_id, status_id, updated_at) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            pstmt.setInt(2, statusId);
            pstmt.setTimestamp(3, Timestamp.valueOf(dateTime));
            pstmt.executeUpdate();
        }
    }

    private static int getShippingIdForOrder(Connection conn, String orderId) throws SQLException {
        String sql = "SELECT shipping_id FROM orders WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("shipping_id") : -1;
            }
        }
    }

    // Métodos utilitarios
    private static void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback", rollbackEx);
            }
        }
    }

    private static void closeConnection(Connection conn, boolean originalAutoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(originalAutoCommit);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    // Interfaces funcionales para las transacciones
    @FunctionalInterface
    private interface TransactionFunction<T> {
        T execute(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    private interface OrderTransactionFunction {
        void execute(Connection conn, Order order) throws SQLException;
    }
}