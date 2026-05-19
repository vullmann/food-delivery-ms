package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record CustomerProfileUpdatedEvent(
        UUID customerId,
        String phone,
        LocalDateTime updatedAt
) {
    public static final String TOPIC = "customer.profile.updated";

    public CustomerProfileUpdatedEvent {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
