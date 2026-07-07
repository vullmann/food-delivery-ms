package de.ullmann.fooddelivery.authservice.dto;

import de.ullmann.fooddelivery.common.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterStaffRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull Role role
) {}
