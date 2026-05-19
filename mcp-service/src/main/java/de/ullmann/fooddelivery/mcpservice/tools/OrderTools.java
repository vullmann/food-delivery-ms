package de.ullmann.fooddelivery.mcpservice.tools;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import de.ullmann.fooddelivery.mcpservice.client.OrderServiceClient;
import de.ullmann.fooddelivery.mcpservice.dto.OrderResponse;

@Service
public class OrderTools {

    private final OrderServiceClient orderServiceClient;

    public OrderTools(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @Tool(description = "Get the details and current status of a specific order by its UUID. " +
            "Returns order status, items, total amount, and timestamps.")
    public String getOrderStatus(String orderId) {
        return orderServiceClient.getOrderById(orderId)
                .map(this::formatOrder)
                .orElse("No order found with ID: " + orderId);
    }

    @Tool(description = "Get all orders placed by a customer using their customer UUID. " +
            "Returns a list of orders with their statuses and details.")
    public String getOrdersByCustomer(String customerId) {
        List<OrderResponse> orders = orderServiceClient.getOrdersByCustomer(customerId);
        if (orders.isEmpty()) {
            return "No orders found for customer ID: " + customerId;
        }
        StringBuilder sb = new StringBuilder("Orders for customer " + customerId + ":\n");
        for (int i = 0; i < orders.size(); i++) {
            sb.append("\nOrder ").append(i + 1).append(":\n").append(formatOrder(orders.get(i)));
        }
        return sb.toString();
    }

    private String formatOrder(OrderResponse o) {
        return "Order ID: " + o.id()
                + "\nStatus: " + o.status()
                + "\nTotal: " + o.totalAmount()
                + "\nPlaced at: " + o.createdAt()
                + "\nItems: " + o.items()
                + "\nRestaurantId: " + o.restaurantId();
    }
}
