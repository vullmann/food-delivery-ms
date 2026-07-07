package de.ullmann.fooddelivery.apigateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    // 32-byte key in Base64: (44/4)*3 - 1 padding = 32 bytes → valid for HMAC-SHA256
    private static final String TEST_SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private static final String TEST_CUSTOMER_ID = "a1b2c3d4-0001-0001-0001-000000000001";
    private static final String TEST_EMAIL = "test@example.com";

    private JwtAuthenticationFilter filter;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secret", TEST_SECRET);
        ReflectionTestUtils.invokeMethod(filter, "init");
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    }

    @Test
    void getOrder_shouldReturnMinusOne() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }

    @Test
    void filter_preflightRequest_shouldPassThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.options("/customers")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    void filter_publicPath_shouldPassThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    void filter_missingAuthorizationHeader_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/customers").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_malformedAuthorizationHeader_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/customers")
                .header(HttpHeaders.AUTHORIZATION, "Token sometoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_validJwt_shouldInjectHeadersAndPassThrough() {
        String token = Jwts.builder()
                .subject(TEST_CUSTOMER_ID)
                .claim("email", TEST_EMAIL)
                .signWith(signingKey)
                .compact();
        MockServerHttpRequest request = MockServerHttpRequest.get("/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            capturedExchange.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get()).isNotNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Id"))
                .isEqualTo(TEST_CUSTOMER_ID);
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Email"))
                .isEqualTo(TEST_EMAIL);
    }

    @Test
    void filter_invalidJwt_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
