package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryOrderAccessDeniedExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UUID id = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
        DeliveryOrderAccessDeniedException ex = new DeliveryOrderAccessDeniedException(id);
        assertThat(ex.getMessage()).contains(id.toString());
    }
}
