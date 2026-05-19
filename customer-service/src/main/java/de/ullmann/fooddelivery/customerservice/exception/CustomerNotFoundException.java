package de.ullmann.fooddelivery.customerservice.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID id) {
        super("Customer not found: " + id);
    }

    public CustomerNotFoundException(String email) {
        super("Customer not found: " + email);
    }
}