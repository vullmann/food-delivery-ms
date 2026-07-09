package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;

// generic fan-out event published by auth-service on every registration (customer or staff);
// role-owning services (customer-service for CUSTOMER, delivery-service for DELIVERY_DRIVER, ...)
// consume it and create their own profile with userId as the primary key
public record UserRegisteredEvent(
        UUID userId,
        String role,
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address,
        LocalDateTime registeredAt
) {
    public static final String TOPIC = "user.registered";

    public UserRegisteredEvent {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");
    }
}
