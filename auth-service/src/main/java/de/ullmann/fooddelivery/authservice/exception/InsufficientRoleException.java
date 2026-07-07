package de.ullmann.fooddelivery.authservice.exception;

public class InsufficientRoleException extends RuntimeException {
    public InsufficientRoleException(String message) {
        super(message);
    }
}
