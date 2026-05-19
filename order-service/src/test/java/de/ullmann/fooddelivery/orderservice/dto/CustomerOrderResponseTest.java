package de.ullmann.fooddelivery.orderservice.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderItem;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;

class CustomerOrderResponseTest {

    @Test
    void from_shouldMapOrderToOrderResponse() {
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");
        List<CustomerOrderItem> items = List.of(
                CustomerOrderItem.create(UUID.randomUUID(), "Pizza", "with salami", 1, new BigDecimal("10.00"))
        );

        CustomerOrder customerOrder = CustomerOrder.create(customerId, restaurantId, address, items);

        CustomerOrderResponse response = CustomerOrderResponse.from(customerOrder);

        assertNotNull(response);
        assertEquals(customerOrder.getId(), response.id());
        assertEquals(customerId, response.customerId());
        assertEquals(restaurantId, response.restaurantId());
        assertEquals(new BigDecimal("10.00"), response.totalAmount());
        assertEquals(CustomerOrderStatus.CREATED, response.status());
        assertEquals(customerOrder.getCreatedAt(), response.createdAt());
    }

    @Test
    void from_shouldHandleMultipleItems() {
        List<CustomerOrderItem> items = List.of(
                CustomerOrderItem.create(UUID.randomUUID(), "Pizza", "with onions", 2, new BigDecimal("10.00")),
                CustomerOrderItem.create(UUID.randomUUID(), "Pasta", "without onions", 1, new BigDecimal("15.00"))
        );

        CustomerOrder customerOrder = CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                items
        );

        CustomerOrderResponse response = CustomerOrderResponse.from(customerOrder);

        assertEquals(new BigDecimal("35.00"), response.totalAmount());
    }

    @Test
    void from_shouldMapDifferentOrderStatuses() {
        CustomerOrder customerOrder = CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                List.of(CustomerOrderItem.create(UUID.randomUUID(), "Item", "description", 1, new BigDecimal("10.00")))
        );

        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        CustomerOrderResponse response = CustomerOrderResponse.from(customerOrder);

        assertEquals(CustomerOrderStatus.PENDING, response.status());
    }
}
