package de.ullmann.fooddelivery.restaurantservice.exception;

import java.util.UUID;

public class RestaurantOrderNotFoundException extends RuntimeException {
    public RestaurantOrderNotFoundException(UUID orderId) {
        super("Restaurant order not found for orderId: " + orderId);
    }
}