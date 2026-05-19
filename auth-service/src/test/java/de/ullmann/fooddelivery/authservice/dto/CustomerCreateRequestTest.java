package de.ullmann.fooddelivery.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerCreateRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        AddressRequest address = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");
        CustomerCreateRequest request = new CustomerCreateRequest("John", "Doe", "john@doe.com", "hashed", "+49123", address);

        assertThat(request.firstName()).isEqualTo("John");
        assertThat(request.lastName()).isEqualTo("Doe");
        assertThat(request.email()).isEqualTo("john@doe.com");
        assertThat(request.password()).isEqualTo("hashed");
        assertThat(request.phone()).isEqualTo("+49123");
        assertThat(request.address()).isEqualTo(address);
    }
}
