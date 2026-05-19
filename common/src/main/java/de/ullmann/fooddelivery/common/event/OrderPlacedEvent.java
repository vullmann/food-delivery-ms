package de.ullmann.fooddelivery.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;

public record OrderPlacedEvent(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        BigDecimal totalAmount,
        List<OrderItemDto> items,
        Address deliveryAddress,
        LocalDateTime createdAt) {

    public static final String TOPIC = "order.placed";

    public OrderPlacedEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(deliveryAddress, "deliveryAddress must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalAmount must not be negative");
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }

        totalAmount = new BigDecimal(totalAmount.stripTrailingZeros().toPlainString());
        items = List.copyOf(items);
    }
}
