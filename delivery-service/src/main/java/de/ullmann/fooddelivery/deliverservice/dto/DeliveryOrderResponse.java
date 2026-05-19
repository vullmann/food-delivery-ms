package de.ullmann.fooddelivery.deliverservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;

public record DeliveryOrderResponse(
        UUID id,
        UUID orderId,
        UUID restaurantId,
        UUID driverId,
        Address pickupAddress,
        Address deliveryAddress,
        DeliveryStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DeliveryOrderResponse from(DeliveryOrder delivery) {
        return new DeliveryOrderResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getRestaurantId(),
                delivery.getDriverId(),
                delivery.getPickupAddress(),
                delivery.getDeliveryAddress(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt());
    }
}
