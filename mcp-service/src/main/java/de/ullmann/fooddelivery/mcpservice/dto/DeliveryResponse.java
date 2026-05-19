package de.ullmann.fooddelivery.mcpservice.dto;

import java.time.LocalDateTime;

public record DeliveryResponse(
        String id,
        String orderId,
        String status,
        String driverId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
