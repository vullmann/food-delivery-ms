package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record DeliveryCancelledEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime cancelledAt
) {
    public static final String TOPIC = "delivery.cancelled";

    public DeliveryCancelledEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(cancelledAt, "cancelledAt must not be null");
    }
}
