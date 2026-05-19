package de.ullmann.fooddelivery.customerservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerRequest(@NotBlank String firstName,
                                    @NotBlank String lastName,
                                    @NotBlank String phone,
                                    @Valid @NotNull AddressRequest address) {
}