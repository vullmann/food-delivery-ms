package de.ullmann.fooddelivery.mcpservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        RestaurantResponse.Address address = new RestaurantResponse.Address("Via Roma", "1", "Rome", "00100", "Italy");
        RestaurantResponse response = new RestaurantResponse("rest-id", "Trattoria", "Italian food", "ITALIAN", true, address);

        assertThat(response.id()).isEqualTo("rest-id");
        assertThat(response.name()).isEqualTo("Trattoria");
        assertThat(response.cuisineType()).isEqualTo("ITALIAN");
        assertThat(response.isOpen()).isTrue();
        assertThat(response.address()).isEqualTo(address);
    }

    @Test
    void address_constructor_shouldSetAllFields() {
        RestaurantResponse.Address address = new RestaurantResponse.Address("Via Roma", "1", "Rome", "00100", "Italy");
        assertThat(address.street()).isEqualTo("Via Roma");
        assertThat(address.city()).isEqualTo("Rome");
    }
}
