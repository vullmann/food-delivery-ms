package de.ullmann.fooddelivery.customerservice.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateCustomerRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidCreateCustomerRequest() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+49123456789",
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_shouldFailWhenPhoneIsBlank() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                null,
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));

    }

    @Test
    void validation_shouldFailWhenFirstNameIsBlank() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "",
                "Doe",
                "john.doe@example.com",
                "+49123456789",
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_shouldFailWhenFirstNameIsNull() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                null,
                "Doe",
                "john.doe@example.com",
                "+49123456789",
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_shouldFailWhenLastNameIsBlank() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "",
                "john.doe@example.com",
                "+49123456789",
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void validation_shouldFailWhenEmailIsBlank() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "",
                "+49123456789",
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validation_shouldFailWhenEmailIsInvalid() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "invalid-email",
                "+49123456789",
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validation_shouldFailWhenAddressIsNull() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+49123456789",
                null
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
    }

    @Test
    void validation_shouldFailWhenAddressIsInvalid() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+49123456789",
                new AddressRequest("", "123", "Berlin", "10115", "Germany")
        );

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("address")));
    }
}
