package de.ullmann.fooddelivery.customerservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.customerservice.entity.Customer;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreatedAt()
        );
    }
}