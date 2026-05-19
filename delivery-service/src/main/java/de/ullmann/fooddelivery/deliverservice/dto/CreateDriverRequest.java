package de.ullmann.fooddelivery.deliverservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDriverRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phone
) {}
