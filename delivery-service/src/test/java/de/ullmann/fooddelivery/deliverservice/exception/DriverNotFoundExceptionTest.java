package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UUID id = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001");
        DriverNotFoundException ex = new DriverNotFoundException(id);
        assertThat(ex.getMessage()).contains(id.toString());
    }
}
