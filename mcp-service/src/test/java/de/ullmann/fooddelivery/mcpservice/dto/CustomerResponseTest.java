package de.ullmann.fooddelivery.mcpservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        CustomerResponse.Address address = new CustomerResponse.Address("Main St", "1", "Berlin", "10115", "Germany");
        CustomerResponse response = new CustomerResponse("cust-id", "John", "Doe", "john@doe.com", "+49123", address);

        assertThat(response.id()).isEqualTo("cust-id");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.email()).isEqualTo("john@doe.com");
        assertThat(response.phone()).isEqualTo("+49123");
        assertThat(response.address()).isEqualTo(address);
    }

    @Test
    void address_constructor_shouldSetAllFields() {
        CustomerResponse.Address address = new CustomerResponse.Address("Main St", "1", "Berlin", "10115", "Germany");
        assertThat(address.street()).isEqualTo("Main St");
        assertThat(address.city()).isEqualTo("Berlin");
    }
}
