package de.ullmann.fooddelivery.authservice.service;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import de.ullmann.fooddelivery.authservice.dto.CustomerCreateRequest;
import de.ullmann.fooddelivery.authservice.dto.CustomerCreateResponse;
import de.ullmann.fooddelivery.authservice.dto.LoginRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginResponse;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerResponse;
import de.ullmann.fooddelivery.authservice.dto.RegisterStaffRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterStaffResponse;
import de.ullmann.fooddelivery.authservice.dto.ValidateResponse;
import de.ullmann.fooddelivery.authservice.entity.UserCredential;
import de.ullmann.fooddelivery.authservice.exception.CustomerServiceException;
import de.ullmann.fooddelivery.authservice.exception.EmailAlreadyRegisteredException;
import de.ullmann.fooddelivery.authservice.exception.InsufficientRoleException;
import de.ullmann.fooddelivery.authservice.exception.InvalidCredentialsException;
import de.ullmann.fooddelivery.authservice.exception.InvalidTokenException;
import de.ullmann.fooddelivery.authservice.repository.UserCredentialRepository;
import de.ullmann.fooddelivery.common.security.Role;
import io.jsonwebtoken.JwtException;

@Service
@Transactional
public class AuthService {

    private final UserCredentialRepository credentialRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RestClient customerClient;

    public AuthService(
            UserCredentialRepository credentialRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            RestClient customerClient) {
        this.credentialRepository = credentialRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.customerClient = customerClient;
    }

    public RegisterCustomerResponse registerCustomer(RegisterCustomerRequest req) {
        if (credentialRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyRegisteredException(req.email());
        }

        String hashed = passwordEncoder.encode(req.password());

        CustomerCreateResponse customer;
        try {
            customer = customerClient.post()
                    .uri("/customers")
                    .body(new CustomerCreateRequest(
                            req.firstName(), req.lastName(), req.email(),
                            hashed, req.phone(), req.address()
                    ))
                    .retrieve()
                    .body(CustomerCreateResponse.class);
        } catch (RestClientException e) {
            throw new CustomerServiceException("Failed to create customer profile: " + e.getMessage());
        }

        UserCredential credential = UserCredential.createCustomer(
                customer.id(), req.email(), hashed, req.firstName(), req.lastName(), req.phone());
        credentialRepository.save(credential);

        return new RegisterCustomerResponse(
                credential.getUserId(), credential.getFirstName(), credential.getLastName(),
                credential.getEmail(), credential.getPhone()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        UserCredential credential = credentialRepository.findByEmail(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), credential.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                jwtService.generateToken(credential.getUserId(), credential.getEmail(), credential.getRole()),
                credential.getUserId(),
                credential.getEmail()
        );
    }

    public RegisterStaffResponse registerStaff(RegisterStaffRequest req) {
        Role callerRole = currentCallerRole();
        assertCanCreate(callerRole, req.role());

        if (credentialRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyRegisteredException(req.email());
        }

        String hashed = passwordEncoder.encode(req.password());
        UserCredential credential = UserCredential.create(
                req.email(), hashed, req.firstName(), req.lastName(), req.phone(), req.role());
        credentialRepository.save(credential);

        return new RegisterStaffResponse(
                credential.getUserId(), credential.getFirstName(), credential.getLastName(),
                credential.getEmail(), credential.getPhone(), credential.getRole());
    }

    private Role currentCallerRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(authority -> Role.valueOf(authority.getAuthority().replace("ROLE_", "")))
                .orElseThrow(() -> new InsufficientRoleException("Caller has no role"));
    }

    private void assertCanCreate(
            Role callerRole,
            Role targetRole) {
        boolean allowed = switch (callerRole) {
            case SUPER_ADMIN -> targetRole == Role.RESTAURANT_ADMIN
                    || targetRole == Role.RESTAURANT_EMPLOYEE
                    || targetRole == Role.DELIVERY_ADMIN
                    || targetRole == Role.DELIVERY_DRIVER;
            case RESTAURANT_ADMIN -> targetRole == Role.RESTAURANT_EMPLOYEE;
            case DELIVERY_ADMIN -> targetRole == Role.DELIVERY_DRIVER;
            default -> false;
        };

        if (!allowed) {
            throw new InsufficientRoleException(callerRole + " is not allowed to create " + targetRole);
        }
    }

    @Transactional(readOnly = true)
    public ValidateResponse validate(String token) {
        try {
            var claims = jwtService.validateToken(token);
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            return new ValidateResponse(userId, email);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }
}
