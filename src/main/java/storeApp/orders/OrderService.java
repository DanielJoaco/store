package storeApp.orders;

import storeApp.product.ProductInfo;
import storeApp.user.Customer;

import java.util.List;
import java.util.Objects;

public class OrderService {

    /**
     * Crea un nuevo pedido validando todos los parámetros.
     */
    public static Order createOrder(
            Customer.CustomerInfo customerInfo,
            List<Order.OrderItem> items,
            Payment.PaymentData paymentData,
            double shippingCost,
            double tax,
            double discount,
            Address shippingAddress,
            String notes
    ) {
        // Aquí vuelves a centralizar validaciones si quieres:
        Objects.requireNonNull(customerInfo,   "customerInfo is required");
        Objects.requireNonNull(items,          "items is required");
        if (items.isEmpty())
            throw new IllegalArgumentException("Order must contain at least one product");
        Objects.requireNonNull(paymentData,    "paymentData is required");
        if (shippingCost < 0 || tax < 0 || discount < 0)
            throw new IllegalArgumentException("Costs and discount must be non-negative");
        Objects.requireNonNull(shippingAddress,"shippingAddress is required");

        // Y delegas en el constructor (ya limpio de lógica compleja):
        return new Order(
                customerInfo,
                items,
                paymentData,
                shippingCost,
                tax,
                discount,
                shippingAddress,
                notes
        );
    }

    /**
     * Añade un producto al pedido y devuelve el subtotal y total recalculados.
     */
    public static void addProduct(Order order, ProductInfo product, int quantity, double unitPrice) {
        Objects.requireNonNull(order,   "order is required");
        Objects.requireNonNull(product, "product is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        if (unitPrice < 0) throw new IllegalArgumentException("Unit total must be >= 0");

        order.addProduct(product, quantity, unitPrice);
        // Si en tu diseño vuelves a subtotal/total mutables, aquí podrías:
        // order.recalculateTotals();
    }

    /**
     * Elimina un producto del pedido.
     */
    public static void removeProduct(Order order, ProductInfo product) {
        Objects.requireNonNull(order,   "order is required");
        Objects.requireNonNull(product, "product is required");

        order.removeProduct(product);
        // si usas recálculo mutante: order.recalculateTotals();
    }

    /**
     * Marca el pedido como enviado, asigna tracking y valida el estado.
     */
    public static void shipOrder(Order order, String trackingNumber) {
        Objects.requireNonNull(order,          "order is required");
        Objects.requireNonNull(trackingNumber,"trackingNumber is required");
        order.ship(trackingNumber);
    }

    /**
     * Marca el pedido como entregado.
     */
    public static void deliverOrder(Order order) {
        Objects.requireNonNull(order, "order is required");
        order.deliver();
    }

    /**
     * Cancela el pedido, validando que no esté ya entregado.
     */
    public static void cancelOrder(Order order) {
        Objects.requireNonNull(order, "order is required");
        order.cancel();
    }

}
