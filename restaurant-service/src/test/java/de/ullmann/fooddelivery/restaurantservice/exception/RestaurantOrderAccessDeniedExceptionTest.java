package de.ullmann.fooddelivery.restaurantservice.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantOrderAccessDeniedExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        RestaurantOrderAccessDeniedException ex = new RestaurantOrderAccessDeniedException(orderId, restaurantId);
        assertThat(ex.getMessage()).contains(orderId.toString());
        assertThat(ex.getMessage()).contains(restaurantId.toString());
    }
}
