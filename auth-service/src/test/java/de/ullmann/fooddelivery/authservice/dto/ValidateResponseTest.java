package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        UUID customerId = UUID.randomUUID();
        ValidateResponse response = new ValidateResponse(customerId, "user@example.com");

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.email()).isEqualTo("user@example.com");
    }
}
