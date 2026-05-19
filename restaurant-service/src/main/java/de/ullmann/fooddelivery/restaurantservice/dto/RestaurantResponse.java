package de.ullmann.fooddelivery.restaurantservice.dto;

import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import de.ullmann.fooddelivery.common.model.Address;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String description,
        Address address,
        String phone,
        String email,
        CuisineType cuisineType,
        boolean isOpen,
        LocalDateTime createdAt
) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getCuisineType(),
                restaurant.isOpen(),
                restaurant.getCreatedAt()
        );
    }
}