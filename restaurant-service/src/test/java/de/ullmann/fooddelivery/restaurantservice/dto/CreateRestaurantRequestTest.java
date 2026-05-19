package de.ullmann.fooddelivery.restaurantservice.dto;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;

import static org.assertj.core.api.Assertions.assertThat;

class CreateRestaurantRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        AddressRequest address = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Trattoria", "Italian food", address, "+49123", "info@trattoria.de", CuisineType.ITALIAN, true);

        assertThat(request.name()).isEqualTo("Trattoria");
        assertThat(request.description()).isEqualTo("Italian food");
        assertThat(request.address()).isEqualTo(address);
        assertThat(request.phone()).isEqualTo("+49123");
        assertThat(request.email()).isEqualTo("info@trattoria.de");
        assertThat(request.cuisineType()).isEqualTo(CuisineType.ITALIAN);
        assertThat(request.isOpen()).isTrue();
    }
}
