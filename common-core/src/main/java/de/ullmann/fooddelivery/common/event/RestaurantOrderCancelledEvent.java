package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record RestaurantOrderCancelledEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime cancelledAt
) {
    public static final String TOPIC = "restaurant.order.cancelled";

    public RestaurantOrderCancelledEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(cancelledAt, "cancelledAt must not be null");
    }
}
