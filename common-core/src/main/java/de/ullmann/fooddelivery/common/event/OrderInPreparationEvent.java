package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderInPreparationEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime startedAt
) {
    public static final String TOPIC = "order.in.preparation";

    public OrderInPreparationEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
    }
}
