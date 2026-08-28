package de.ullmann.fooddelivery.restaurantservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderItem;

public record RestaurantOrderItemResponse(
        UUID menuItemId,
        String name,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
    public static RestaurantOrderItemResponse from(RestaurantOrderItem item) {
        return new RestaurantOrderItemResponse(
                item.getMenuItemId(),
                item.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
