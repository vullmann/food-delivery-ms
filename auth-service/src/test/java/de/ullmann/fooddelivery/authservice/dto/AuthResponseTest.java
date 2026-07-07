package de.ullmann.fooddelivery.authservice.dto;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        UUID userId = UUID.randomUUID();
        AuthResponse response = new AuthResponse("jwt-token", userId, "user@example.com");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
    }
}
