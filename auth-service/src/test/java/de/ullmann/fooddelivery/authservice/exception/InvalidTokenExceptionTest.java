package de.ullmann.fooddelivery.authservice.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidTokenExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        InvalidTokenException ex = new InvalidTokenException();
        assertThat(ex.getMessage()).isNotBlank();
    }
}
