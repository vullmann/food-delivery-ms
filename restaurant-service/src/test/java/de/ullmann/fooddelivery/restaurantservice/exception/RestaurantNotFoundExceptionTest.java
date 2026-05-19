package de.ullmann.fooddelivery.restaurantservice.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UUID id = UUID.randomUUID();
        RestaurantNotFoundException ex = new RestaurantNotFoundException(id);
        assertThat(ex.getMessage()).contains(id.toString());
    }
}
