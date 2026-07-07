package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderDeliveredEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime deliveredAt
) {
    public static final String TOPIC = "order.delivered";

    public OrderDeliveredEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(deliveredAt, "deliveredAt must not be null");
    }
}
