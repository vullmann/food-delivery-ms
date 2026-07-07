package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

import de.ullmann.fooddelivery.common.security.Role;

public record StaffResponse(UUID userId, String email, Role role) {}
