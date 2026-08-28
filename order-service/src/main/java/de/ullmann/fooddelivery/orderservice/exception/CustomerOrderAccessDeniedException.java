package de.ullmann.fooddelivery.orderservice.exception;

import java.util.UUID;

public class CustomerOrderAccessDeniedException extends RuntimeException {
    public CustomerOrderAccessDeniedException(UUID id) {
        super("Not authorized to access this resource: " + id);
    }
}
