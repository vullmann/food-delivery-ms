package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record DriverAssignedEvent(
        UUID orderId,
        UUID customerId,
        LocalDateTime assignedAt
) {
    public static final String TOPIC = "driver.assigned";

    public DriverAssignedEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(assignedAt, "assignedAt must not be null");
    }
}
