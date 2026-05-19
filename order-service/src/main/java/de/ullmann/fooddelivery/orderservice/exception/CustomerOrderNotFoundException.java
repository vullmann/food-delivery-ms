package de.ullmann.fooddelivery.orderservice.exception;

import java.util.UUID;

public class CustomerOrderNotFoundException extends RuntimeException {
    public CustomerOrderNotFoundException(UUID id) {
        super("Order not found: " + id);
    }
}