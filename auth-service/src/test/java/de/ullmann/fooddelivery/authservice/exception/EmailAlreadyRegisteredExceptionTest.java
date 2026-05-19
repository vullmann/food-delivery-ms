package de.ullmann.fooddelivery.authservice.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAlreadyRegisteredExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        EmailAlreadyRegisteredException ex = new EmailAlreadyRegisteredException("user@example.com");
        assertThat(ex.getMessage()).contains("user@example.com");
    }
}
