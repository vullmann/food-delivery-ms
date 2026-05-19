package de.ullmann.fooddelivery.restaurantservice.exception;

import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(UUID id) {
        super("MenuItem not found with id: " + id);
    }
}