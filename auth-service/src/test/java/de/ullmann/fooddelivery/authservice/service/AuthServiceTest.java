package de.ullmann.fooddelivery.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.ullmann.fooddelivery.authservice.dto.AddressRequest;
import de.ullmann.fooddelivery.authservice.dto.AuthResponse;
import de.ullmann.fooddelivery.authservice.dto.CustomerCreateRequest;
import de.ullmann.fooddelivery.authservice.dto.CustomerCreateResponse;
import de.ullmann.fooddelivery.authservice.dto.LoginRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterRequest;
import de.ullmann.fooddelivery.authservice.dto.ValidateResponse;
import de.ullmann.fooddelivery.authservice.entity.UserCredential;
import de.ullmann.fooddelivery.authservice.exception.CustomerServiceException;
import de.ullmann.fooddelivery.authservice.exception.EmailAlreadyRegisteredException;
import de.ullmann.fooddelivery.authservice.exception.InvalidCredentialsException;
import de.ullmann.fooddelivery.authservice.exception.InvalidTokenException;
import de.ullmann.fooddelivery.authservice.repository.UserCredentialRepository;
import de.ullmann.fooddelivery.common.security.Role;
import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCredentialRepository credentialRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RestClient customerClient;

    private AuthService authService;

    private ObjectMapper objectMapper;

    private MockRestServiceServer server;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "user@example.com";
    private static final AddressRequest ADDRESS = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");

    @BeforeEach
    void setUp() {
        // Initialize ObjectMapper and register the Java 8 Time module for LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 1. Grab a clean, standard builder shell
        RestClient.Builder builder = RestClient.builder();

        // 2. Bind the Mock Server directly to that builder shell
        server = MockRestServiceServer.bindTo(builder).build();

        // 3. Construct the real client directly, bypassing Spring's @Qualifier entirely!
        customerClient = builder.build();

        authService = new AuthService(credentialRepository, jwtService, passwordEncoder, customerClient);

    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrow() {
        when(credentialRepository.existsByEmail(EMAIL)).thenReturn(true);
        RegisterRequest req = new RegisterRequest("John", "Doe", EMAIL, "secret", "+49123", ADDRESS);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void register_success_shouldReturnAuthResponse() {
        when(credentialRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(jwtService.generateToken(any(), anyString(), any())).thenReturn("jwt-token");

        // 1. create the chain mocks
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        // 2. Define the chain behavior
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(CustomerCreateRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        CustomerCreateResponse customerResponse = new CustomerCreateResponse(USER_ID, "John", "Doe", EMAIL,
                "+49123", ADDRESS);
        when(responseSpec.body(CustomerCreateResponse.class)).thenReturn(customerResponse);

        AuthService service = new AuthService(credentialRepository, jwtService, passwordEncoder, restClient);
        RegisterRequest req = new RegisterRequest("John", "Doe", EMAIL, "secret", "+49123", ADDRESS);

        AuthResponse response = service.register(req);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.email()).isEqualTo(EMAIL);
    }

    @Test
    void login_whenCredentialNotFound_shouldThrow() {
        when(credentialRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        LoginRequest req = new LoginRequest(EMAIL, "secret");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_whenPasswordMismatch_shouldThrow() {
        UserCredential credential = UserCredential.createCustomer(USER_ID, EMAIL, "hashed");
        when(credentialRepository.findByEmail(EMAIL)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);
        LoginRequest req = new LoginRequest(EMAIL, "wrongpassword");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_success_shouldReturnAuthResponse() {
        UserCredential credential = UserCredential.createCustomer(USER_ID, EMAIL, "hashed");
        when(credentialRepository.findByEmail(EMAIL)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtService.generateToken(USER_ID, EMAIL, Role.CUSTOMER)).thenReturn("jwt-token");
        LoginRequest req = new LoginRequest(EMAIL, "secret");

        AuthResponse response = authService.login(req);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(USER_ID);
    }

    @Test
    void validate_validToken_shouldReturnValidateResponse() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(USER_ID.toString());
        when(claims.get("email", String.class)).thenReturn(EMAIL);
        when(jwtService.validateToken("valid-token")).thenReturn(claims);

        ValidateResponse response = authService.validate("valid-token");

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.email()).isEqualTo(EMAIL);
    }

    @Test
    void validate_invalidToken_shouldThrowInvalidTokenException() {
        when(jwtService.validateToken("bad-token")).thenThrow(new io.jsonwebtoken.JwtException("bad"));

        assertThatThrownBy(() -> authService.validate("bad-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void validate_malformedSubject_shouldThrowInvalidTokenException() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("not-a-uuid");
        when(jwtService.validateToken("malformed-subject-token")).thenReturn(claims);

        assertThatThrownBy(() -> authService.validate("malformed-subject-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void register_restClientException_shouldThrowCustomerServiceException() {
        // 1. Mock Server Expectations: Simulate a server crash on the endpoint
        server.expect(MockRestRequestMatchers.requestTo("/customers"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // 2. Act & Assert: Verify your custom exception class is thrown
        RegisterRequest req = new RegisterRequest("John", "Doe", EMAIL, "secret", "+49123", ADDRESS);

        CustomerServiceException exception = assertThrows(CustomerServiceException.class, () -> {
            authService.register(req);
        });

        // 3. Assert: Verify your custom error message format is preserved
        assertTrue(exception.getMessage().contains("Failed to create customer profile:"));

        server.verify();
    }
}
