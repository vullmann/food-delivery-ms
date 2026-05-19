package de.ullmann.fooddelivery.restaurantservice.dto;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateRestaurantRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        AddressRequest address = new AddressRequest("New St", "5", "Munich", "80331", "Germany");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest(
                "Sushi Bar", "Fresh sushi", address, "+49999", "sushi@bar.de", CuisineType.SUSHI, false);

        assertThat(request.name()).isEqualTo("Sushi Bar");
        assertThat(request.description()).isEqualTo("Fresh sushi");
        assertThat(request.address()).isEqualTo(address);
        assertThat(request.cuisineType()).isEqualTo(CuisineType.SUSHI);
        assertThat(request.isOpen()).isFalse();
    }
}
