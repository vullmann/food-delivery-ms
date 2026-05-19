package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.UUID;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(UUID id) {
        super("Driver not found: " + id);
    }
}
