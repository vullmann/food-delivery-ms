package de.ullmann.fooddelivery.restaurantservice.dto;

import java.math.BigDecimal;

import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMenuItemRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull MenuItemCategory category,
        boolean available
) {
}