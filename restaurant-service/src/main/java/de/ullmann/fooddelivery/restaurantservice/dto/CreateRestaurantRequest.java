package de.ullmann.fooddelivery.restaurantservice.dto;

import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRestaurantRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Valid AddressRequest address,
        @NotBlank String phone,
        @NotBlank @Email String email,
        @NotNull CuisineType cuisineType,
        boolean isOpen
) {
}