package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

public record RegisterCustomerResponse(UUID userId, String firstName, String lastName, String email, String phone) {}
