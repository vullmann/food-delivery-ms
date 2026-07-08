package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

import de.ullmann.fooddelivery.common.security.Role;

public record RegisterStaffResponse(UUID userId, String firstName, String lastName, String email, String phone, Role role) {}
