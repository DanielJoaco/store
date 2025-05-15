package storeApp.db;

import storeApp.orders.Address;
import storeApp.orders.Order;
import storeApp.orders.Payment;
import java.sql.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    /**
     * Creates a new order in the database
     * @param order The order to create
     * @return true if the operation was successful, false otherwise
     */
    public static boolean createOrder(Order order) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Get or insert payment method ID
            int paymentMethodId = getPaymentMethodId(conn, order.getPaymentData().paymentMethod().name());
            if (paymentMethodId == -1) {
                throw new SQLException("Failed to get payment method ID");
            }
            int franchiseId = order.getPaymentData().franchises() == null ? 0 : getFranchiseId(conn, order.getPaymentData().franchises().name());
            // Get street ID
            int streetId = getStreetId(conn, order.getShippingAddress().st().name());
            if (streetId == -1) {
                throw new SQLException("Failed to get street ID");
            }

            // Insert shipping address and get its ID
            int shippingAddressId = insertShippingAddress(conn, order.getShippingAddress(), streetId);
            if (shippingAddressId == -1) {
                throw new SQLException("Failed to insert shipping address");
            }

            // Get user ID
            int userId = getUserId(conn, order.getCustomerInfo().email());
            if (userId == -1) {
                throw new SQLException("User not found");
            }

            // Get status ID
            int statusId = getStatusId(conn, order.getLastStatus().status().name());
            if (statusId == -1) {
                throw new SQLException("Invalid order status");
            }

            // Insert order
            String sqlOrder = """
                INSERT INTO orders (id, user_id, order_date, status_id, payment_id, franchise_id,
                subtotal, shipping_cost, tax, discount, total, shipping_id, tracking_number, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder)) {
                pstmt.setString(1, order.getId());
                pstmt.setInt(2, userId);
                pstmt.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
                pstmt.setInt(4, statusId);
                pstmt.setInt(5, paymentMethodId);
                pstmt.setInt(6, franchiseId);
                pstmt.setDouble(7, order.getSubtotal());
                pstmt.setDouble(8, order.getShippingCost());
                pstmt.setDouble(9, order.getTax());
                pstmt.setDouble(10, order.getDiscount());
                pstmt.setDouble(11, order.getTotal());
                pstmt.setInt(12, shippingAddressId);
                pstmt.setString(13, order.getTrackingNumber());
                pstmt.setString(14, order.getNotes());

                pstmt.executeUpdate();
            }
            // Insert order items
            insertOrderItems(conn, order.getId(), order.getItems());

            // Insert status history
            insertStatusHistory(conn, order.getId(), statusId, order.getLastStatus().dateTime());

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error creating order", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Updates an existing order in the database
     * @param order The updated order information
     * @return true if the operation was successful, false otherwise
     */
    public static boolean editOrder(Order order) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Get shipping ID for the order
            int shippingId = getShippingIdForOrder(conn, order.getId());
            if (shippingId == -1) {
                throw new SQLException("Order shipping address not found");
            }

            // Get street ID
            int streetId = getStreetId(conn, order.getShippingAddress().st().name());
            if (streetId == -1) {
                throw new SQLException("Failed to get street ID");
            }

            // Update shipping address
            updateShippingAddress(conn, shippingId, streetId, order.getShippingAddress());

            // Get payment method ID
            int paymentMethodId = getPaymentMethodId(conn, order.getPaymentData().paymentMethod().name());
            if (paymentMethodId == -1) {
                throw new SQLException("Failed to get payment method ID");
            }
            // Get Franchise ID
            int franchiseId = order.getPaymentData().franchises() == null ? -1 : getFranchiseId(conn, order.getPaymentData().franchises().name());

            // Get status ID
            int statusId = getStatusId(conn, order.getLastStatus().status().name());
            if (statusId == -1) {
                throw new SQLException("Invalid order status");
            }

            // Update order
            String sqlOrder = """
                UPDATE orders
                SET order_date = ?, status_id = ?, payment_id = ?, franchise_id = ?, subtotal = ?,
                shipping_cost = ?, tax = ?, discount = ?, total = ?,
                tracking_number = ?, notes = ?
                WHERE id = ?
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
                pstmt.setInt(2, statusId);
                pstmt.setInt(3, paymentMethodId);
                pstmt.setInt(4, franchiseId);
                pstmt.setDouble(5, order.getSubtotal());
                pstmt.setDouble(6, order.getShippingCost());
                pstmt.setDouble(7, order.getTax());
                pstmt.setDouble(8, order.getDiscount());
                pstmt.setDouble(9, order.getTotal());
                pstmt.setString(    10, order.getTrackingNumber());
                pstmt.setString(11, order.getNotes());
                pstmt.setString(12, order.getId());

                pstmt.executeUpdate();
            }

            // Delete existing items and insert new ones
            deleteOrderItems(conn, order.getId());
            insertOrderItems(conn, order.getId(), order.getItems());

            // Add new status history entry
            insertStatusHistory(conn, order.getId(), statusId, order.getLastStatus().dateTime());

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error updating order", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Updates only the shipping address of an order
     * @param orderId The ID of the order to update
     * @param shippingAddress The updated shipping address
     * @return true if the operation was successful, false otherwise
     */
    public static boolean updateOrderAddress(String orderId, Address shippingAddress) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Get shipping ID for the order
            int shippingId = getShippingIdForOrder(conn, orderId);
            if (shippingId == -1) {
                throw new SQLException("Order shipping address not found");
            }

            // Get street ID
            int streetId = getStreetId(conn, shippingAddress.st().name());
            if (streetId == -1) {
                throw new SQLException("Failed to get street ID");
            }

            // Update shipping address
            updateShippingAddress(conn, shippingId, streetId, shippingAddress);

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error updating order address", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Updates the status of an order and adds a history entry
     * @param orderId The ID of the order to update
     * @param status The new status
     * @param dateTime The timestamp for the status change
     * @return true if the operation was successful, false otherwise
     */
    public static boolean updateOrderStatus(String orderId, Order.Status status, java.time.LocalDateTime dateTime) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Get status ID
            int statusId = getStatusId(conn, status.name());
            if (statusId == -1) {
                throw new SQLException("Invalid order status");
            }

            // Update order status
            String sqlUpdateStatus = "UPDATE orders SET status_id = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateStatus)) {
                pstmt.setInt(1, statusId);
                pstmt.setString(2, orderId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Order not found");
                }
            }

            // Add status history entry
            insertStatusHistory(conn, orderId, statusId, dateTime);

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error updating order status", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Updates the payment method for an order
     * @param orderId The ID of the order to update
     * @param paymentMethod The new payment method
     * @param franchise The new franchise (can be null depending on payment method)
     * @return true if the operation was successful, false otherwise
     */
    public static boolean updateOrderPayment(String orderId, Payment.PaymentMethod paymentMethod, Payment.Franchises franchise) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Get payment method ID
            int paymentMethodId = getPaymentMethodId(conn, paymentMethod.name());
            if (paymentMethodId == -1) {
                throw new SQLException("Failed to get payment method ID");
            }
            int franchiseId = franchise == null ? -1 : getFranchiseId(conn, franchise.name());

            // Update order payment
            String sqlUpdatePayment = "UPDATE orders SET payment_id = ?, franchise_id = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePayment)) {
                pstmt.setInt(1, paymentMethodId);
                pstmt.setInt(2, franchiseId);
                pstmt.setString(3, orderId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Order not found");
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error updating order payment", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Updates the items in an order
     * @param orderId The ID of the order to update
     * @param items The new list of order items
     * @return true if the operation was successful, false otherwise
     */
    public static boolean updateOrderItems(String orderId, List<Order.OrderItem> items) {
        Connection conn = null;
        boolean originalAutoCommit = false;

        try {
            conn = DatabaseManager.connect();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Verify order exists
            if (!orderExists(conn, orderId)) {
                throw new SQLException("Order not found");
            }

            // Delete existing items
            deleteOrderItems(conn, orderId);

            // Insert new items
            insertOrderItems(conn, orderId, items);

            // Recalculate order totals
            updateOrderTotals(conn, orderId, items);

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error updating order items", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            closeConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Recalculates and updates the order totals based on the items
     */
    private static void updateOrderTotals(Connection conn, String orderId, List<Order.OrderItem> items) throws SQLException {
        double subtotal = 0.0;
        for (Order.OrderItem item : items) {
            subtotal += item.quantity() * item.unitPrice();
        }

        // Get current order values to maintain other fields
        String sqlGetOrder = "SELECT shipping_cost, tax, discount FROM orders WHERE id = ?";
        double shippingCost = 0.0;
        double tax = 0.0;
        double discount = 0.0;

        try (PreparedStatement pstmt = conn.prepareStatement(sqlGetOrder)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    shippingCost = rs.getDouble("shipping_cost");
                    tax = rs.getDouble("tax");
                    discount = rs.getDouble("discount");
                }
            }
        }

        // Calculate total
        double total = subtotal + shippingCost + tax - discount;

        // Update order totals
        String sqlUpdateTotals = "UPDATE orders SET subtotal = ?, total = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateTotals)) {
            pstmt.setDouble(1, subtotal);
            pstmt.setDouble(2, total);
            pstmt.setString(3, orderId);
            pstmt.executeUpdate();
        }
    }

    private static void updateProductStock(Connection conn, String productId, int quantity) throws SQLException {
        String sqlUpdateStock = "UPDATE products SET stock = stock - ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateStock)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to get a payment method ID from the database
     */
    private static int getPaymentMethodId(Connection conn, String methodName) throws SQLException {
        String sql = """
                SELECT pm.id
                FROM payment_data pm
                WHERE pm.methods_name = ?
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, methodName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
        return -1;
    }

    private static int getFranchiseId(Connection conn, String franchiseName) throws SQLException {
        String sql = """
                SELECT f.id
                FROM franchises f
                WHERE f.name = ?
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, franchiseName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
        return 0;
    }

    /**
     * Helper method to get a street ID from the database
     */
    private static int getStreetId(Connection conn, String streetName) throws SQLException {
        String sql = "SELECT id FROM streets WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, streetName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    /**
     * Helper method to insert a shipping address and return its ID
     */
    private static int insertShippingAddress(Connection conn, Address address, int streetId) throws SQLException {
        String sql = """
            INSERT INTO shipping_address (street_id, st_number, st_letter, cross_st_number,
            cross_st_letter, house_number, house_letter, indications, city, postal_code, state, country)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            commitShippingAddress(address, streetId, pstmt);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    private static void commitShippingAddress(Address address, int streetId, PreparedStatement pstmt) throws SQLException {
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

    /**
     * Helper method to update a shipping address
     */
    private static void updateShippingAddress(Connection conn, int shippingId, int streetId,
                                              Address address) throws SQLException {
        String sql = """
            UPDATE shipping_address
            SET street_id = ?, st_number = ?, st_letter = ?, cross_st_number = ?,
            cross_st_letter = ?, house_number = ?, house_letter = ?, indications = ?,
            city = ?, postal_code = ?, state = ?, country = ?
            WHERE id = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            commitShippingAddress(address, streetId, pstmt);
            pstmt.setInt(13, shippingId);

            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to get a user ID from an email
     */
    private static int getUserId(Connection conn, String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    /**
     * Helper method to get a status ID from a status name
     */
    private static int getStatusId(Connection conn, String statusName) throws SQLException {
        String sql = "SELECT id FROM order_statuses WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statusName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    /**
     * Helper method to insert order items
     */
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

    /**
     * Helper method to delete all items for an order
     */
    private static void deleteOrderItems(Connection conn, String orderId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to insert a status history entry
     */
    private static void insertStatusHistory(Connection conn, String orderId, int statusId,
                                            java.time.LocalDateTime dateTime) throws SQLException {
        String sql = "INSERT INTO status_history (order_id, status_id, updated_at) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            pstmt.setInt(2, statusId);
            pstmt.setTimestamp(3, Timestamp.valueOf(dateTime));
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to get the shipping ID for an order
     */
    private static int getShippingIdForOrder(Connection conn, String orderId) throws SQLException {
        String sql = "SELECT shipping_id FROM orders WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shipping_id");
                }
            }
        }
        return -1;
    }

    /**
     * Helper method to check if an order exists
     */
    private static boolean orderExists(Connection conn, String orderId) throws SQLException {
        String sql = "SELECT 1 FROM orders WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Helper method to close a database connection and restore autocommit
     */
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
}