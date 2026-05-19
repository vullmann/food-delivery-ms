package de.ullmann.fooddelivery.restaurantservice.dto;

import de.ullmann.fooddelivery.restaurantservice.entity.MenuItem;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        UUID restaurantId,
        String name,
        String description,
        BigDecimal price,
        MenuItemCategory category,
        boolean available
) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getRestaurant().getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.isAvailable()
        );
    }
}