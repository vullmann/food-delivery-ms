package de.ullmann.fooddelivery.authservice.service;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void generateToken_shouldReturnNonBlankToken() {
        UUID customerId = UUID.randomUUID();
        String token = jwtService.generateToken(customerId, "user@example.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void validateToken_shouldReturnClaimsWithCorrectSubjectAndEmail() {
        UUID customerId = UUID.randomUUID();
        String email = "user@example.com";
        String token = jwtService.generateToken(customerId, email);

        Claims claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo(customerId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo(email);
    }

    @Test
    void validateToken_withInvalidToken_shouldThrowException() {
        assertThatThrownBy(() -> jwtService.validateToken("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }
}
