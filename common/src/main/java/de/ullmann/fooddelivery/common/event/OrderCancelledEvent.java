package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        LocalDateTime cancelledAt
) {
    public static final String TOPIC = "order.cancelled";

    public OrderCancelledEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(cancelledAt, "cancelledAt must not be null");
    }
}
