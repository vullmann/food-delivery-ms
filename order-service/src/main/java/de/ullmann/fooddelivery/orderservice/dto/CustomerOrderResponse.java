package de.ullmann.fooddelivery.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;

public record CustomerOrderResponse(
        UUID id,
        UUID customerId,
        UUID restaurantId,
        BigDecimal totalAmount,
        CustomerOrderStatus status,
        LocalDateTime createdAt,
        List<CustomerOrderItemResponse> items
) {
    public static CustomerOrderResponse from(CustomerOrder customerOrder) {
        return new CustomerOrderResponse(
                customerOrder.getId(),
                customerOrder.getCustomerId(),
                customerOrder.getRestaurantId(),
                customerOrder.getTotalAmount(),
                customerOrder.getStatus(),
                customerOrder.getCreatedAt(),
                customerOrder.getItems().stream().map(CustomerOrderItemResponse::from).toList()
        );
    }
}
