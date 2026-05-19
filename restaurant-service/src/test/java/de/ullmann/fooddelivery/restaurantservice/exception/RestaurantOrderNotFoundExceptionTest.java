package de.ullmann.fooddelivery.restaurantservice.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantOrderNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UUID orderId = UUID.randomUUID();
        RestaurantOrderNotFoundException ex = new RestaurantOrderNotFoundException(orderId);
        assertThat(ex.getMessage()).contains(orderId.toString());
    }
}
