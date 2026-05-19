package de.ullmann.fooddelivery.authservice.dto;

public record CustomerCreateRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone,
        AddressRequest address
) {}
