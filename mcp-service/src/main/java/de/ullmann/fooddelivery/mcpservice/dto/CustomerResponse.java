package de.ullmann.fooddelivery.mcpservice.dto;

public record CustomerResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address
) {
    public record Address(String street, String houseNumber, String city, String zip, String country) {}
}
