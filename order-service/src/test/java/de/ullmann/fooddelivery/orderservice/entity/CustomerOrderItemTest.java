package de.ullmann.fooddelivery.orderservice.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerOrderItemTest {

    @Test
    void create_shouldCreateOrderItemWithCorrectValues() {
        UUID menuItemId = UUID.randomUUID();
        String name = "Pizza Margherita";
        String description = "The best pizza in town";
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("12.50");

        CustomerOrderItem item = CustomerOrderItem.create(menuItemId, name, description, quantity, price);

        assertNotNull(item);
        assertNull(item.getId());
        assertEquals(menuItemId, item.getMenuItemId());
        assertEquals(name, item.getName());
        assertEquals(description, item.getDescription());
        assertEquals(quantity, item.getQuantity());
        assertEquals(price, item.getUnitPrice());
        assertEquals(price.multiply(BigDecimal.valueOf(quantity)), item.getTotalPrice());

        assertNull(item.getCustomerOrder());
    }

    @Test
    void getSubtotal_shouldHandleSingleQuantity() {
        CustomerOrderItem item = CustomerOrderItem.create(
                UUID.randomUUID(),
                "Salad",
                "great food",
                1,
                new BigDecimal("6.99")
        );

        BigDecimal subtotal = item.getTotalPrice();

        assertEquals(new BigDecimal("6.99"), subtotal);
    }

    @Test
    void getSubtotal_shouldHandleLargeQuantity() {
        CustomerOrderItem item = CustomerOrderItem.create(
                UUID.randomUUID(),
                "Soda",
                "small",
                10,
                new BigDecimal("2.50")
        );

        BigDecimal subtotal = item.getTotalPrice();

        assertEquals(new BigDecimal("25.00"), subtotal);
    }

    @Test
    void assignToOrder_shouldSetCustomerOrderReference() {
        CustomerOrderItem item = CustomerOrderItem.create(
                UUID.randomUUID(),
                "Test Item",
                "Test Description",
                1,
                BigDecimal.TEN
        );

        List<CustomerOrderItem> items = Arrays.asList(
                CustomerOrderItem.create(UUID.randomUUID(), "Pizza", "The best pizza in town", 1,
                        new BigDecimal("10.00")),
                CustomerOrderItem.create(UUID.randomUUID(), "Pasta", "The best pizza in town", 2,
                        new BigDecimal("8.00"))
        );

        CustomerOrder customerOrder = CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                items
        );

        item.assignToCustomerOrder(customerOrder);

        assertNotNull(item.getCustomerOrder());
        assertEquals(customerOrder, item.getCustomerOrder());
    }
}
