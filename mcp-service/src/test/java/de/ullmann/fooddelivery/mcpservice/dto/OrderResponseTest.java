package de.ullmann.fooddelivery.mcpservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        OrderResponse response = new OrderResponse("order-id", "cust-id", "rest-id", "PLACED",
                BigDecimal.valueOf(25.00), List.of(), now);

        assertThat(response.id()).isEqualTo("order-id");
        assertThat(response.status()).isEqualTo("PLACED");
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
        assertThat(response.createdAt()).isEqualTo(now);
    }
}
