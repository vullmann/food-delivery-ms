package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

public record LoginResponse(String token, UUID userId, String email) {
}
