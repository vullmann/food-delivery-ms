package de.ullmann.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OrderItemDto(
                UUID menuItemId,
                String name,
                Integer quantity,
                BigDecimal price
) {
    public OrderItemDto {
        Objects.requireNonNull(menuItemId, "menuItemId must not be null");
        Objects.requireNonNull(name,       "name must not be null");
        Objects.requireNonNull(quantity,   "quantity must not be null");
        Objects.requireNonNull(price,      "price must not be null");
        if (name.isBlank())                         throw new IllegalArgumentException("name must not be blank");
        if (quantity < 1)                           throw new IllegalArgumentException("quantity must be >= 1");
        if (price.compareTo(BigDecimal.ZERO) < 0)  throw new IllegalArgumentException("price must be >= 0");
        price = new BigDecimal(price.stripTrailingZeros().toPlainString());
    }
}