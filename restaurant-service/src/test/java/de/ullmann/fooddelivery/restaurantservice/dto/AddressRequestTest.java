package de.ullmann.fooddelivery.restaurantservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        AddressRequest request = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");

        assertThat(request.street()).isEqualTo("Main St");
        assertThat(request.houseNumber()).isEqualTo("1");
        assertThat(request.city()).isEqualTo("Berlin");
        assertThat(request.zip()).isEqualTo("10115");
        assertThat(request.country()).isEqualTo("Germany");
    }
}
