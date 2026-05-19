package de.ullmann.fooddelivery.mcpservice.tools;

import de.ullmann.fooddelivery.mcpservice.client.DeliveryServiceClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class DeliveryTools {

    private final DeliveryServiceClient deliveryServiceClient;

    public DeliveryTools(DeliveryServiceClient deliveryServiceClient) {
        this.deliveryServiceClient = deliveryServiceClient;
    }

    @Tool(description = "Get the delivery status for an order using the order's UUID. " +
            "Returns delivery status, assigned driver ID, and timestamps.")
    public String getDeliveryStatus(String orderId) {
        return deliveryServiceClient.getDeliveryByOrderId(orderId)
                .map(d -> "Delivery ID: " + d.id()
                        + "\nOrder ID: " + d.orderId()
                        + "\nStatus: " + d.status()
                        + "\nDriver ID: " + d.driverId()
                        + "\nCreated at: " + d.createdAt()
                        + "\nUpdated at: " + d.updatedAt())
                .orElse("No delivery found for order ID: " + orderId);
    }
}
