package de.ullmann.fooddelivery.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String houseNumber,
        @NotBlank String city,
        @NotBlank @Size(max = 10) @Pattern(regexp = "\\d+", message = "zip must contain digits only") String zip,
        @NotBlank String country
) {}