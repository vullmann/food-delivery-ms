package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.UUID;

public class DeliveryOrderAccessDeniedException extends RuntimeException {
    public DeliveryOrderAccessDeniedException(UUID deliveryId) {
        super("Not authorized to access delivery order: " + deliveryId);
    }
}
