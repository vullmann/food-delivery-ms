package de.ullmann.fooddelivery.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderItem;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerOrderItemResponseTest {

    @Test
    void from_shouldMapAllFieldsFromItem() {
        UUID menuItemId = UUID.randomUUID();
        CustomerOrderItem item = CustomerOrderItem.create(
                menuItemId, "Margherita", "Classic pizza", 2, new BigDecimal("9.50"));

        CustomerOrderItemResponse response = CustomerOrderItemResponse.from(item);

        assertThat(response.menuItemId()).isEqualTo(menuItemId);
        assertThat(response.menuItemName()).isEqualTo("Margherita");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.unitPrice()).isEqualByComparingTo(new BigDecimal("9.50"));
        assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("19.00"));
    }

    @Test
    void create_shouldStoreAllFields() {
        UUID menuItemId = UUID.randomUUID();
        BigDecimal unit = new BigDecimal("5.00");
        BigDecimal total = new BigDecimal("10.00");

        CustomerOrderItemResponse response = new CustomerOrderItemResponse(menuItemId, "Burger", 2, unit, total);

        assertThat(response.menuItemId()).isEqualTo(menuItemId);
        assertThat(response.menuItemName()).isEqualTo("Burger");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.unitPrice()).isEqualByComparingTo(unit);
        assertThat(response.totalPrice()).isEqualByComparingTo(total);
    }
}
