package de.ullmann.fooddelivery.orderservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddressRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidAddressRequest() {
        AddressRequest request = new AddressRequest(
                "Main Street",
                "123",
                "Berlin",
                "10115",
                "Germany"
        );

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_shouldFailWhenStreetIsBlank() {
        AddressRequest request = new AddressRequest("", "123", "Berlin", "10115", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("street")));
    }

    @Test
    void validation_shouldFailWhenStreetIsNull() {
        AddressRequest request = new AddressRequest(null, "123", "Berlin", "10115", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("street")));
    }

    @Test
    void validation_shouldFailWhenHouseNumberIsBlank() {
        AddressRequest request = new AddressRequest("Main St", "", "Berlin", "10115", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("houseNumber")));
    }

    @Test
    void validation_shouldFailWhenCityIsBlank() {
        AddressRequest request = new AddressRequest("Main St", "123", "", "10115", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")));
    }

    @Test
    void validation_shouldFailWhenZipIsBlank() {
        AddressRequest request = new AddressRequest("Main St", "123", "Berlin", "", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("zip")));
    }

    @Test
    void validation_shouldFailWhenCountryIsBlank() {
        AddressRequest request = new AddressRequest("Main St", "123", "Berlin", "10115", "");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("country")));
    }

    @Test
    void validation_shouldFailWhenZipContainsNonDigits() {
        AddressRequest request = new AddressRequest("Main St", "123", "Berlin", "ABC123", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("zip") &&
            v.getMessage().equals("zip must contain digits only")));
    }

    @Test
    void validation_shouldFailWhenZipExceedsMaxSize() {
        AddressRequest request = new AddressRequest("Main St", "123", "Berlin", "12345678901", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("zip")));
    }

    @Test
    void validation_shouldPassWithMaxSizeZip() {
        AddressRequest request = new AddressRequest("Main St", "123", "Berlin", "1234567890", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_shouldPassWithShortZip() {
        AddressRequest request = new AddressRequest("Main St", "123", "Berlin", "123", "Germany");

        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
