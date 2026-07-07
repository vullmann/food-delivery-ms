package de.ullmann.fooddelivery.authservice.dto;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ValidateResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        UUID userId = UUID.randomUUID();
        ValidateResponse response = new ValidateResponse(userId, "user@example.com");

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
    }
}
