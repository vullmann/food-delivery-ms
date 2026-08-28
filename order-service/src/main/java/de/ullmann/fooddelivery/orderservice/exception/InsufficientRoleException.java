package de.ullmann.fooddelivery.orderservice.exception;

public class InsufficientRoleException extends RuntimeException {
    public InsufficientRoleException(String message) {
        super(message);
    }
}
