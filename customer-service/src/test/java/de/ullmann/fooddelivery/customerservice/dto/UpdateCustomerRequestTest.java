package de.ullmann.fooddelivery.customerservice.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UpdateCustomerRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidUpdateCustomerRequest() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "John",
                "Doe",
                "+49123456789",
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    private static @NonNull AddressRequest getAddressRequest(String streetName) {
        return new AddressRequest(streetName, "123", "Berlin", "10115", "Germany");
    }

    @Test
    void validation_shouldFailWhenPhoneIsBlank() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "",
                "Doe",
                "",
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void validation_shouldFailWhenPhoneIsNull() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                null,
                "Doe",
                null,
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void validation_shouldFailWhenFirstNameIsBlank() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "",
                "Doe",
                "+49123456789",
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_shouldFailWhenFirstNameIsNull() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                null,
                "Doe",
                "+49123456789",
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_shouldFailWhenLastNameIsBlank() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "John",
                "",
                "+49123456789",
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void validation_shouldFailWhenLastNameIsNull() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "John",
                null,
                "+49123456789",
                getAddressRequest("Main St")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void validation_shouldFailWhenAddressIsNull() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "John",
                "Doe",
                "+49123456789",
                null
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
    }

    @Test
    void validation_shouldFailWhenAddressIsInvalid() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "John",
                "Doe",
                "+49123456789",
                getAddressRequest("")
        );

        Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("address")));
    }
}
