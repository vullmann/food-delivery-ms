package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

public record ValidateResponse(UUID customerId, String email) {}
