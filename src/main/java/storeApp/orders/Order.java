package storeApp.orders;

import storeApp.product.ProductInfo;
import storeApp.user.Customer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static storeApp.menu.utils.Utils.capitalize;

public class Order {

    public enum Status {
        PENDING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    private final String id;
    private Customer.CustomerInfo customerInfo;
    private final List<OrderItem> items;
    private final LocalDateTime orderDate;
    private Status status;
    private final Payment.PaymentData paymentData;
    private double shippingCost;
    private double tax;
    private double discount;
    private Address shippingAddress;
    private String trackingNumber;
    private final List<StatusHistory> statusHistory;
    private String notes;

    public Order(
            Customer.CustomerInfo customerInfo,
            List<OrderItem> products,
            Payment.PaymentData paymentData,
            double shippingCost,
            double tax,
            double discount,
            Address shippingAddress,
            String notes) {

        Objects.requireNonNull(customerInfo, "customerInfo is required");
        Objects.requireNonNull(products, "products is required");
        Objects.requireNonNull(paymentData, "paymentData is required");
        Objects.requireNonNull(shippingAddress, "shippingAddress is required");

        if (products.isEmpty()) throw new IllegalArgumentException("Order must contain at least one product");
        if (shippingCost < 0 || tax < 0 || discount < 0)
            throw new IllegalArgumentException("Costs and discount must be non-negative");

        this.id = UUID.randomUUID().toString();
        this.customerInfo = customerInfo;
        this.items = new ArrayList<>(products);
        this.orderDate = LocalDateTime.now();
        this.status = Status.PENDING;
        this.paymentData = paymentData;
        this.shippingCost = shippingCost;
        this.tax = tax;
        this.discount = discount;
        this.shippingAddress = shippingAddress;
        this.trackingNumber = null;
        this.statusHistory = new ArrayList<>();
        this.statusHistory.add(new StatusHistory(status, orderDate));
        this.notes = notes;
    }

    public Order(
            String id,
            Customer.CustomerInfo customerInfo,
            List<OrderItem> products,
            LocalDateTime orderDate,
            Status status,
            List<Order.StatusHistory> statusHistory,
            Payment.PaymentData paymentData,
            double shippingCost,
            double tax,
            double discount,
            Address shippingAddress,
            String trackingNumber,
            String notes) {
        if(id == null || id.isEmpty())
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        Objects.requireNonNull(customerInfo, "customerInfo is required");
        Objects.requireNonNull(products, "products is required");
        Objects.requireNonNull(orderDate, "orderDate is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(paymentData, "paymentData is required");
        Objects.requireNonNull(shippingAddress, "shippingAddress is required");
        if(statusHistory.isEmpty())
            throw new IllegalArgumentException("Order status history is required");

        if (products.isEmpty()) throw new IllegalArgumentException("Order must contain at least one product");
        if (shippingCost < 0 || tax < 0 || discount < 0)
            throw new IllegalArgumentException("Costs and discount must be non-negative");

        this.id = id;
        this.customerInfo = customerInfo;
        this.items = new ArrayList<>(products);
        this.orderDate = orderDate;
        this.status = status;
        this.paymentData = paymentData;
        this.shippingCost = shippingCost;
        this.tax = tax;
        this.discount = discount;
        this.shippingAddress = shippingAddress;
        this.trackingNumber = trackingNumber;
        this.statusHistory = new ArrayList<>(statusHistory);
        this.notes = notes;
    }

    public void addProduct(ProductInfo product, String id, int quantity, double unitPrice) {
        OrderItem orderItem = new OrderItem(product, id, quantity, unitPrice);
        items.add(orderItem);
    }

    public void removeProduct(ProductInfo product) {
        items.removeIf(item -> item.productInfo().equals(product));
    }

    private void setStatus(Status status) {
        this.status = status;
        LocalDateTime lastUpdated = LocalDateTime.now();
        this.statusHistory.add(new StatusHistory(status, lastUpdated));
    }
    public void ship(String trackingNumber) {
        if (status != Status.PENDING)
            throw new IllegalStateException("Only PENDING orders can be shipped");
        setStatus(Status.SHIPPED);
        this.trackingNumber = trackingNumber;
    }
    public void deliver() {
        if (status != Status.SHIPPED)
            throw new IllegalStateException("Only SHIPPED orders can be delivered");
        setStatus(Status.DELIVERED);
    }
    public void cancel() {
        if (status == Status.DELIVERED)
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        setStatus(Status.CANCELLED);
    }

    public void setCustomerInfo(Customer.CustomerInfo customerInfo) {this.customerInfo = customerInfo;}
    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }
    public void setTax(double tax) {
        this.tax = tax;
    }
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    public void setShippingAddress(Address shippingAddress) {this.shippingAddress = shippingAddress;}
    public void setNotes(String notes) {this.notes = notes;}

    public String getId() {
        String[] id_split = id.split("-");
        return id_split[4];
    }
    public Customer.CustomerInfo getCustomerInfo() {return customerInfo;}
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    public String getOrderDateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        return orderDate.format(formatter);
    }
    public LocalDateTime getOrderDate() {return orderDate;}
    public String getCustomerName() {
        return customerInfo.name();
    }
    public Payment.PaymentData getPaymentData() {return paymentData;}
    public double getSubtotal() {
        return items.stream()
                .mapToDouble(i -> i.unitPrice() * i.quantity())
                .sum();
    }
    public double getShippingCost() {return shippingCost;}
    public double getTax() {return tax;}
    public double getDiscount() {return discount;}
    public double getTotal() {
        return getSubtotal() + shippingCost + tax - discount;
    }
    public int getItemCount() {
        return items.stream()
                .mapToInt(OrderItem::quantity)
                .sum();
    }
    public Address getShippingAddress() {return shippingAddress;}
    public String getTrackingNumber() {return trackingNumber;}
    public StatusHistory getLastStatus() {
        return statusHistory.getLast();
    }
    public String getLastStatusString(){return capitalize(statusHistory.getLast().status.name());}
    public List<StatusHistory> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);}
    public String getNotes() {return notes;}

    public record OrderItem(
            ProductInfo productInfo,
            String productId,
            int quantity,
            double unitPrice)
    {}
    public record StatusHistory(
            Status status,
            LocalDateTime dateTime)
    {}
}
