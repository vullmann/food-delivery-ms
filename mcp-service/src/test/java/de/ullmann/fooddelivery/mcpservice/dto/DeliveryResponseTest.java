package de.ullmann.fooddelivery.mcpservice.dto;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        DeliveryResponse response = new DeliveryResponse("del-id", "ord-id", "PENDING", "drv-id", now, now);

        assertThat(response.id()).isEqualTo("del-id");
        assertThat(response.orderId()).isEqualTo("ord-id");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.driverId()).isEqualTo("drv-id");
    }
}
