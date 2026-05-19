package de.ullmann.fooddelivery.mcpservice.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String menuItemId,
        String menuItemName,
        int quantity,
        BigDecimal unitPrice
) {}
