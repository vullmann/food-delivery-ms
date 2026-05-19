package de.ullmann.fooddelivery.authservice.dto;

import java.util.UUID;

public record CustomerCreateResponse(UUID id, String firstName, String lastName, String email, String phone, AddressRequest address) {}
