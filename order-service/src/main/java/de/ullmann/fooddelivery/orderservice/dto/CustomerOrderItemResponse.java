package de.ullmann.fooddelivery.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderItem;

public record CustomerOrderItemResponse(
        UUID menuItemId,
        String menuItemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
    public static CustomerOrderItemResponse from(CustomerOrderItem item) {
        return new CustomerOrderItemResponse(
                item.getMenuItemId(),
                item.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
