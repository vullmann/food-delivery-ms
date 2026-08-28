package de.ullmann.fooddelivery.authservice.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.authservice.dto.AddressRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginResponse;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerResponse;
import de.ullmann.fooddelivery.authservice.dto.RegisterStaffRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterStaffResponse;
import de.ullmann.fooddelivery.authservice.dto.UserCredentialResponse;
import de.ullmann.fooddelivery.authservice.dto.ValidateResponse;
import de.ullmann.fooddelivery.authservice.entity.UserCredential;
import de.ullmann.fooddelivery.authservice.exception.EmailAlreadyRegisteredException;
import de.ullmann.fooddelivery.authservice.exception.InsufficientRoleException;
import de.ullmann.fooddelivery.authservice.exception.InvalidCredentialsException;
import de.ullmann.fooddelivery.authservice.exception.InvalidTokenException;
import de.ullmann.fooddelivery.authservice.repository.UserCredentialRepository;
import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.common.security.Role;
import io.jsonwebtoken.JwtException;

@Service
@Transactional
public class AuthService {

    private static final String AGGREGATE_TYPE = "UserCredential";

    private final UserCredentialRepository credentialRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OutboxEventService outboxEventService;

    public AuthService(
            UserCredentialRepository credentialRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            OutboxEventService outboxEventService) {
        this.credentialRepository = credentialRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.outboxEventService = outboxEventService;
    }

    public RegisterCustomerResponse registerCustomer(RegisterCustomerRequest req) {
        if (credentialRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyRegisteredException(req.email());
        }

        String hashed = passwordEncoder.encode(req.password());
        UUID userId = UUID.randomUUID();

        UserCredential credential = UserCredential.createCustomer(
                userId, req.email(), hashed, req.firstName(), req.lastName(), req.phone());
        credentialRepository.save(credential);

        publishUserRegistered(credential, toAddress(req.address()));

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

        publishUserRegistered(credential, null);

        return new RegisterStaffResponse(
                credential.getUserId(), credential.getFirstName(), credential.getLastName(),
                credential.getEmail(), credential.getPhone(), credential.getRole());
    }

    @Transactional(readOnly = true)
    public List<UserCredentialResponse> getAllUsers() {
        Role callerRole = currentCallerRole();
        if (callerRole != Role.SUPER_ADMIN) {
            throw new InsufficientRoleException(callerRole + " is not allowed to list all users");
        }

        return credentialRepository.findAll().stream()
                .map(c -> new UserCredentialResponse(
                        c.getUserId(), c.getFirstName(), c.getLastName(), c.getEmail(), c.getPhone(),
                        c.getRole(), c.getCreatedAt()))
                .toList();
    }

    private void publishUserRegistered(UserCredential credential, Address address) {
        outboxEventService.createEvent(
                AGGREGATE_TYPE,
                credential.getUserId(),
                UserRegisteredEvent.TOPIC,
                new UserRegisteredEvent(
                        credential.getUserId(),
                        credential.getRole().name(),
                        credential.getFirstName(),
                        credential.getLastName(),
                        credential.getEmail(),
                        credential.getPhone(),
                        address,
                        LocalDateTime.now(ZoneOffset.UTC)
                )
        );
    }

    private Address toAddress(AddressRequest r) {
        return Address.of(r.street(), r.houseNumber(), r.city(), r.zip(), r.country());
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
