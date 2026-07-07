package de.ullmann.fooddelivery.authservice.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.security.Role;

class UserCredentialTest {

    @Test
    void create_Customer_shouldSetAllFields() {
        UUID userId = UUID.randomUUID();
        UserCredential credential = UserCredential.createCustomer(userId, "user@example.com", "hashedPw123");

        assertThat(credential.getId()).isNotNull();
        assertThat(credential.getUserId()).isEqualTo(userId);
        assertThat(credential.getEmail()).isEqualTo("user@example.com");
        assertThat(credential.getHashedPassword()).isEqualTo("hashedPw123");
        assertThat(credential.getRole()).isEqualTo(Role.CUSTOMER);
    }
}
