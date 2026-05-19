package de.ullmann.fooddelivery.chatservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UnauthorizedExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        UnauthorizedException ex = new UnauthorizedException("test message");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("test message");
    }
}
