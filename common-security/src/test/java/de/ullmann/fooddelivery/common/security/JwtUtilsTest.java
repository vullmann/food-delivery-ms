package de.ullmann.fooddelivery.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtUtilsTest {

    private static final String SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
    }

    @Test
    void parseClaims_withValidToken_returnsClaims() {
        String token = Jwts.builder()
                .subject("user-123")
                .claim("role", "CUSTOMER")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        Claims claims = jwtUtils.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user-123");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
    }

    @Test
    void parseClaims_withInvalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtUtils.parseClaims("not.a.valid.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getSigningKey_returnsNonNullKey() {
        assertThat(jwtUtils.getSigningKey()).isNotNull();
    }
}
