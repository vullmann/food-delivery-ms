package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

public record AuthResponse(String token, UUID customerId, String email) {}
