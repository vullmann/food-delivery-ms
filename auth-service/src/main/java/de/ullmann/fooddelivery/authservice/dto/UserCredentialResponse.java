package de.ullmann.fooddelivery.authservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import de.ullmann.fooddelivery.common.security.Role;

public record UserCredentialResponse(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        LocalDateTime createdAt) {
}
