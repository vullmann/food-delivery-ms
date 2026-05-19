package de.ullmann.fooddelivery.authservice.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidCredentialsExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException();
        assertThat(ex.getMessage()).isNotBlank();
    }
}
