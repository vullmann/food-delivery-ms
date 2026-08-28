package de.ullmann.fooddelivery.restaurantservice.exception;

public class InsufficientRoleException extends RuntimeException {
    public InsufficientRoleException(String message) {
        super(message);
    }
}
