package de.ullmann.fooddelivery.authservice.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerServiceExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        CustomerServiceException ex = new CustomerServiceException("downstream error");
        assertThat(ex.getMessage()).isEqualTo("downstream error");
    }
}
