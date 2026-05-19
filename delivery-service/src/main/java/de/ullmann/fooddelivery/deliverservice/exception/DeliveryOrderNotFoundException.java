package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.UUID;

public class DeliveryOrderNotFoundException extends RuntimeException {
    public DeliveryOrderNotFoundException(UUID id) {
        super("Delivery order not found: " + id);
    }
}
