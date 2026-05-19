package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryOrderNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UUID id = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        DeliveryOrderNotFoundException ex = new DeliveryOrderNotFoundException(id);
        assertThat(ex.getMessage()).contains(id.toString());
    }
}
