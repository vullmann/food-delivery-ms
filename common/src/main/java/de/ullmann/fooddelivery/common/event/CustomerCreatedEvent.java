package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;

// can be used for welcome mail
public record CustomerCreatedEvent(
        UUID customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address,
        LocalDateTime createdAt
) {
    public static final String TOPIC = "customer.created";

    public CustomerCreatedEvent {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}