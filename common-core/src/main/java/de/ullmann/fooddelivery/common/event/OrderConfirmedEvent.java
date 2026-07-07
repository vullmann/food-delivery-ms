package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderConfirmedEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime confirmedAt
) {
    public static final String TOPIC = "order.confirmed";

    public OrderConfirmedEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(confirmedAt, "confirmedAt must not be null");
    }
}
