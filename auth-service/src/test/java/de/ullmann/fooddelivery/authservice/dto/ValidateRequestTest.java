package de.ullmann.fooddelivery.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateRequestTest {

    @Test
    void constructor_shouldSetToken() {
        ValidateRequest request = new ValidateRequest("some.jwt.token");
        assertThat(request.token()).isEqualTo("some.jwt.token");
    }
}
