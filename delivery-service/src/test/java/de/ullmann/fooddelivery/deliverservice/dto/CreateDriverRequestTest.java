package de.ullmann.fooddelivery.deliverservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDriverRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        CreateDriverRequest request = new CreateDriverRequest("John", "Doe", "+49123456789");

        assertThat(request.firstName()).isEqualTo("John");
        assertThat(request.lastName()).isEqualTo("Doe");
        assertThat(request.phone()).isEqualTo("+49123456789");
    }
}
