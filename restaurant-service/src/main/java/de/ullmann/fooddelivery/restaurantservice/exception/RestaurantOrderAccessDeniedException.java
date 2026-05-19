package de.ullmann.fooddelivery.restaurantservice.exception;

import java.util.UUID;

public class RestaurantOrderAccessDeniedException extends RuntimeException {
    public RestaurantOrderAccessDeniedException(UUID orderId, UUID restaurantId) {
        super("Order " + orderId + " does not belong to restaurant " + restaurantId);
    }
}
