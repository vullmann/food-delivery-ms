package de.ullmann.fooddelivery.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CustomerOrderItemRequest(
        @NotNull UUID menuItemId,
        @NotBlank String name,
        @NotBlank String description,
        @Min(1) int quantity,
        @Positive @NotNull BigDecimal price
) {
}
