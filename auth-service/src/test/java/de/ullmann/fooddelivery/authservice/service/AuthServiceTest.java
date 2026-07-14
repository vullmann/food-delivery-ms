package de.ullmann.fooddelivery.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.ullmann.fooddelivery.authservice.dto.AddressRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginResponse;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerResponse;
import de.ullmann.fooddelivery.authservice.dto.UserCredentialResponse;
import de.ullmann.fooddelivery.authservice.dto.ValidateResponse;
import de.ullmann.fooddelivery.authservice.entity.UserCredential;
import de.ullmann.fooddelivery.authservice.exception.EmailAlreadyRegisteredException;
import de.ullmann.fooddelivery.authservice.exception.InsufficientRoleException;
import de.ullmann.fooddelivery.authservice.exception.InvalidCredentialsException;
import de.ullmann.fooddelivery.authservice.exception.InvalidTokenException;
import de.ullmann.fooddelivery.authservice.repository.UserCredentialRepository;
import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
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

    @Mock
    private OutboxEventService outboxEventService;

    private AuthService authService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "user@example.com";
    private static final AddressRequest ADDRESS = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");

    @BeforeEach
    void setUp() {
        authService = new AuthService(credentialRepository, jwtService, passwordEncoder, outboxEventService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Role role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void registerCustomer_whenEmailAlreadyExists_shouldThrow() {
        when(credentialRepository.existsByEmail(EMAIL)).thenReturn(true);
        RegisterCustomerRequest req = new RegisterCustomerRequest("John", "Doe", EMAIL, "secret", "+49123", ADDRESS);

        assertThatThrownBy(() -> authService.registerCustomer(req))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void registerCustomer_success_shouldReturnRegisterCustomerResponseAndPublishEvent() {
        when(credentialRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        RegisterCustomerRequest req = new RegisterCustomerRequest("John", "Doe", EMAIL, "secret", "+49123", ADDRESS);

        RegisterCustomerResponse response = authService.registerCustomer(req);

        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.userId()).isNotNull();

        verify(credentialRepository).save(any(UserCredential.class));
        verify(outboxEventService).createEvent(
                eq("UserCredential"), eq(response.userId()), eq(UserRegisteredEvent.TOPIC), any(UserRegisteredEvent.class));
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
        UserCredential credential = UserCredential.createCustomer(USER_ID, EMAIL, "hashed", "John", "Doe", "+49123");
        when(credentialRepository.findByEmail(EMAIL)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);
        LoginRequest req = new LoginRequest(EMAIL, "wrongpassword");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_success_shouldReturnLoginResponse() {
        UserCredential credential = UserCredential.createCustomer(USER_ID, EMAIL, "hashed", "John", "Doe", "+49123");
        when(credentialRepository.findByEmail(EMAIL)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtService.generateToken(USER_ID, EMAIL, Role.CUSTOMER)).thenReturn("jwt-token");
        LoginRequest req = new LoginRequest(EMAIL, "secret");

        LoginResponse response = authService.login(req);

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
    void getAllUsers_asSuperAdmin_shouldReturnAllUsers() {
        authenticateAs(Role.SUPER_ADMIN);
        UserCredential credential = UserCredential.createCustomer(USER_ID, EMAIL, "hashed", "John", "Doe", "+49123");
        when(credentialRepository.findAll()).thenReturn(List.of(credential));

        List<UserCredentialResponse> response = authService.getAllUsers();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).userId()).isEqualTo(USER_ID);
        assertThat(response.get(0).email()).isEqualTo(EMAIL);
        assertThat(response.get(0).role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void getAllUsers_asNonSuperAdmin_shouldThrowInsufficientRoleException() {
        authenticateAs(Role.RESTAURANT_ADMIN);

        assertThatThrownBy(() -> authService.getAllUsers())
                .isInstanceOf(InsufficientRoleException.class);
    }
}
