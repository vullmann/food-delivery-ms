package de.ullmann.fooddelivery.orderservice.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CustomerOrderItemRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidOrderItemRequest() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "Pizza Margherita",
                "the best in town",
                2,
                new BigDecimal("12.50")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_shouldFailWhenMenuItemIdIsNull() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                null,
                "Pizza",
                "the best in town",
                2,
                new BigDecimal("12.50")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("menuItemId")));
    }

    @Test
    void validation_shouldFailWhenNameIsBlank() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "",
                "the best in town",
                2,
                new BigDecimal("12.50")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void validation_shouldFailWhenNameIsNull() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                null,
                "the best in town",
                2,
                new BigDecimal("12.50")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void validation_shouldFailWhenQuantityIsZero() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "Pizza",
                "the best in town",
                0,
                new BigDecimal("12.50")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
    }

    @Test
    void validation_shouldFailWhenQuantityIsNegative() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "Pizza",
                "the best in town",
                -1,
                new BigDecimal("12.50")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
    }

    @Test
    void validation_shouldFailWhenPriceIsNull() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "Pizza",
                "the best in town",
                2,
                null
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void validation_shouldFailWhenPriceIsZero() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "Pizza",
                "the best in town",
                2,
                BigDecimal.ZERO
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void validation_shouldFailWhenPriceIsNegative() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "Pizza",
                "the best in town",
                2,
                new BigDecimal("-5.00")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void validation_shouldPassWithMinimumValidValues() {
        CustomerOrderItemRequest request = new CustomerOrderItemRequest(
                UUID.randomUUID(),
                "A",
                "the best in town",
                1,
                new BigDecimal("0.01")
        );

        Set<ConstraintViolation<CustomerOrderItemRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
