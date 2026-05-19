package de.ullmann.fooddelivery.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        AddressRequest address = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@doe.com", "secret", "+49123", address);

        assertThat(request.firstName()).isEqualTo("John");
        assertThat(request.lastName()).isEqualTo("Doe");
        assertThat(request.email()).isEqualTo("john@doe.com");
        assertThat(request.password()).isEqualTo("secret");
        assertThat(request.phone()).isEqualTo("+49123");
        assertThat(request.address()).isEqualTo(address);
    }
}
