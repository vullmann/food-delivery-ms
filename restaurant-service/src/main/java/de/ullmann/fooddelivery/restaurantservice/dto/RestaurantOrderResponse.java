package de.ullmann.fooddelivery.restaurantservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderStatus;

public record RestaurantOrderResponse(
        UUID id,
        UUID customerOrderId,
        UUID restaurantId,
        UUID customerId,
        Address deliveryAddress,
        RestaurantOrderStatus status,
        LocalDateTime createdAt,
        List<RestaurantOrderItemResponse> items
) {
    public static RestaurantOrderResponse from(RestaurantOrder order) {
        return new RestaurantOrderResponse(
                order.getId(),
                order.getCustomerOrderId(),
                order.getRestaurantId(),
                order.getCustomerId(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().stream().map(RestaurantOrderItemResponse::from).toList()
        );
    }
}
