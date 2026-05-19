package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;

public record OrderReadyForDeliveryEvent(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        Address pickupAddress,
        Address deliveryAddress,
        LocalDateTime readyAt
) {
    public static final String TOPIC = "order.ready.for.delivery";

    public OrderReadyForDeliveryEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        Objects.requireNonNull(pickupAddress, "pickupAddress must not be null");
        Objects.requireNonNull(deliveryAddress, "deliveryAddress must not be null");
        Objects.requireNonNull(readyAt, "readyAt must not be null");
    }
}
