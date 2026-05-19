package de.ullmann.fooddelivery.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_shouldReturnBCryptEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertThat(encoder).isNotNull();
        String encoded = encoder.encode("testpassword");
        assertThat(encoder.matches("testpassword", encoded)).isTrue();
    }
}
