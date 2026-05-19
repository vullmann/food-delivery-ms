package de.ullmann.fooddelivery.authservice.entity;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialTest {

    @Test
    void create_shouldSetAllFields() {
        UUID customerId = UUID.randomUUID();
        UserCredential credential = UserCredential.create(customerId, "user@example.com", "hashedPw123");

        assertThat(credential.getId()).isNotNull();
        assertThat(credential.getCustomerId()).isEqualTo(customerId);
        assertThat(credential.getEmail()).isEqualTo("user@example.com");
        assertThat(credential.getHashedPassword()).isEqualTo("hashedPw123");
    }
}
