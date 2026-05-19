package de.ullmann.fooddelivery.restaurantservice.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantOrderItemTest {

    @Test
    void create_shouldSetAllFieldsAndCalculateTotalPrice() {
        UUID menuItemId = UUID.randomUUID();
        BigDecimal unitPrice = BigDecimal.valueOf(10.00);

        RestaurantOrderItem item = RestaurantOrderItem.create(menuItemId, "Burger", 3, unitPrice);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getMenuItemId()).isEqualTo(menuItemId);
        assertThat(item.getName()).isEqualTo("Burger");
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getUnitPrice()).isEqualByComparingTo(unitPrice);
        assertThat(item.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
    }
}
