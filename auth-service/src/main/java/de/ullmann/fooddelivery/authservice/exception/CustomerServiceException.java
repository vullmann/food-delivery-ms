package de.ullmann.fooddelivery.authservice.exception;

public class CustomerServiceException extends RuntimeException {
    public CustomerServiceException(String message) {
        super(message);
    }
}
