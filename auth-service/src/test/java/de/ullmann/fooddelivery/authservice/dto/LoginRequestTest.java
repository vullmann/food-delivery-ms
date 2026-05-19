package de.ullmann.fooddelivery.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        assertThat(request.email()).isEqualTo("user@example.com");
        assertThat(request.password()).isEqualTo("password123");
    }
}
