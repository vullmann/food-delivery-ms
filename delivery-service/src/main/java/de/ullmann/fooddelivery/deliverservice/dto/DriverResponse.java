package de.ullmann.fooddelivery.deliverservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;

public record DriverResponse(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        DriverStatus status,
        LocalDateTime createdAt
) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getPhone(),
                driver.getStatus(),
                driver.getCreatedAt());
    }
}
