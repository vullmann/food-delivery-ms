package de.ullmann.fooddelivery.authservice.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
    }
}
