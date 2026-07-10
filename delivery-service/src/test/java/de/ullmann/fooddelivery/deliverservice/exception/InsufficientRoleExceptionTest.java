package de.ullmann.fooddelivery.deliverservice.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsufficientRoleExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        InsufficientRoleException ex = new InsufficientRoleException("not allowed");
        assertThat(ex.getMessage()).isEqualTo("not allowed");
    }
}
