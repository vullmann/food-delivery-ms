package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerCreateResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        AddressRequest address = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");
        CustomerCreateResponse response = new CustomerCreateResponse(id, "John", "Doe", "john@doe.com", "+49123", address);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.email()).isEqualTo("john@doe.com");
    }
}
