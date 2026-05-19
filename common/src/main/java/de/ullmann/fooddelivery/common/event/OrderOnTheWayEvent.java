package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderOnTheWayEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime pickedUpAt
) {
    public static final String TOPIC = "order.on.the.way";

    public OrderOnTheWayEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(pickedUpAt, "pickedUpAt must not be null");
    }
}
