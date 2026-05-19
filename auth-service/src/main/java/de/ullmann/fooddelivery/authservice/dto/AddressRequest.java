package de.ullmann.fooddelivery.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String houseNumber,
        @NotBlank String city,
        @NotBlank @Size(max = 10) @Pattern(regexp = "\\d+") String zip,
        @NotBlank String country
) {}
