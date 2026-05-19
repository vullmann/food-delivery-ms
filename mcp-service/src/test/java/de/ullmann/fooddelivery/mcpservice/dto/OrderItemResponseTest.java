package de.ullmann.fooddelivery.mcpservice.dto;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        OrderItemResponse item = new OrderItemResponse("menu-id", "Pizza", 2, BigDecimal.valueOf(9.50));

        assertThat(item.menuItemId()).isEqualTo("menu-id");
        assertThat(item.menuItemName()).isEqualTo("Pizza");
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(9.50));
    }
}
