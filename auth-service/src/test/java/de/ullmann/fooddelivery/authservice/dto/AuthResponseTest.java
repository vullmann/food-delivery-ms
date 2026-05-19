package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        UUID customerId = UUID.randomUUID();
        AuthResponse response = new AuthResponse("jwt-token", customerId, "user@example.com");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.email()).isEqualTo("user@example.com");
    }
}
