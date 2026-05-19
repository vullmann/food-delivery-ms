package de.ullmann.fooddelivery.chatservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.chatservice.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final SecretKey KEY = Keys.hmacShaKeyFor(new byte[32]); // 256-bit zero key for tests
    private static final String BASE64_SECRET = Base64.getEncoder().encodeToString(KEY.getEncoded());

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, BASE64_SECRET);
    }

    @Test
    void extractCustomerId_shouldReturnSubjectFromValidToken() {
        String customerId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(customerId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(KEY)
                .compact();

        String result = jwtService.extractCustomerId(token);

        assertThat(result).isEqualTo(customerId);
    }

    @Test
    void extractCustomerId_shouldThrowUnauthorizedExceptionForInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractCustomerId("not.a.valid.token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    void extractCustomerId_shouldThrowUnauthorizedExceptionForExpiredToken() {
        String token = Jwts.builder()
                .subject("some-id")
                .issuedAt(new Date(0))
                .expiration(new Date(1))
                .signWith(KEY)
                .compact();

        assertThatThrownBy(() -> jwtService.extractCustomerId(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    void extractCustomerId_shouldThrowUnauthorizedExceptionForWrongSignature() {
        SecretKey otherKey = Keys.hmacShaKeyFor(new byte[32]);
        // Fill with 1s so it differs from the zero key used to sign
        byte[] bytes = new byte[32];
        java.util.Arrays.fill(bytes, (byte) 1);
        otherKey = Keys.hmacShaKeyFor(bytes);

        String token = Jwts.builder()
                .subject("id")
                .signWith(otherKey)
                .compact();

        assertThatThrownBy(() -> jwtService.extractCustomerId(token))
                .isInstanceOf(UnauthorizedException.class);
    }
}
