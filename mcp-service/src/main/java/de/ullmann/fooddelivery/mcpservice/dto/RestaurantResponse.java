package de.ullmann.fooddelivery.mcpservice.dto;

public record RestaurantResponse(
        String id,
        String name,
        String description,
        String cuisineType,
        boolean isOpen,
        Address address
) {
    public record Address(String street, String houseNumber, String city, String zip, String country) {}
}
