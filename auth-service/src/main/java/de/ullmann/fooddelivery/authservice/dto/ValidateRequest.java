package de.ullmann.fooddelivery.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateRequest(@NotBlank String token) {}
