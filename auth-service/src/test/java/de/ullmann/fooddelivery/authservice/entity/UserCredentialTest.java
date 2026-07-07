package de.ullmann.fooddelivery.authservice.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.security.Role;

class UserCredentialTest {

    @Test
    void create_Customer_shouldSetAllFields() {
        UUID userId = UUID.randomUUID();
        UserCredential credential = UserCredential.createCustomer(
                userId, "user@example.com", "hashedPw123", "John", "Doe", "+49123");

        assertThat(credential.getId()).isNotNull();
        assertThat(credential.getUserId()).isEqualTo(userId);
        assertThat(credential.getEmail()).isEqualTo("user@example.com");
        assertThat(credential.getHashedPassword()).isEqualTo("hashedPw123");
        assertThat(credential.getFirstName()).isEqualTo("John");
        assertThat(credential.getLastName()).isEqualTo("Doe");
        assertThat(credential.getPhone()).isEqualTo("+49123");
        assertThat(credential.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void create_Staff_shouldSetAllFieldsAndDefaultUserIdToId() {
        UserCredential credential = UserCredential.create(
                "staff@example.com", "hashedPw123", "Jane", "Roe", "+49456", Role.DELIVERY_DRIVER);

        assertThat(credential.getId()).isNotNull();
        assertThat(credential.getUserId()).isEqualTo(credential.getId());
        assertThat(credential.getEmail()).isEqualTo("staff@example.com");
        assertThat(credential.getHashedPassword()).isEqualTo("hashedPw123");
        assertThat(credential.getFirstName()).isEqualTo("Jane");
        assertThat(credential.getLastName()).isEqualTo("Roe");
        assertThat(credential.getPhone()).isEqualTo("+49456");
        assertThat(credential.getRole()).isEqualTo(Role.DELIVERY_DRIVER);
    }
}
