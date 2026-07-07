package de.ullmann.fooddelivery.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import de.ullmann.fooddelivery.common.security.Role;
import io.jsonwebtoken.Claims;

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
    void generateToken_shouldEmbedSubjectEmailAndRole() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        String token = jwtService.generateToken(userId, email, Role.CUSTOMER);

        Claims claims = jwtService.validateToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void generateToken_withSuperAdminRole_shouldEmbedCorrectRole() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "admin@example.com", Role.SUPER_ADMIN);

        Claims claims = jwtService.validateToken(token);
        assertThat(claims.get("role", String.class)).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void validateToken_shouldReturnClaimsWithCorrectSubjectAndEmailAndRole() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String token = jwtService.generateToken(userId, email, Role.SUPER_ADMIN);

        Claims claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void validateToken_withInvalidToken_shouldThrowJwtException() {
        assertThatThrownBy(() -> jwtService.validateToken("invalid.token.here"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
