package de.ullmann.fooddelivery.orderservice.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateCustomerOrderRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidCreateOrderRequest() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "with pepper", 1,
                        new BigDecimal("10.00")))
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_shouldFailWhenCustomerIdIsNull() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                null,
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "small", 1, new BigDecimal("10.00")))
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerId")));
    }

    @Test
    void validation_shouldFailWhenRestaurantIdIsNull() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                null,
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "large", 1, new BigDecimal("10.00")))
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("restaurantId")));
    }

    @Test
    void validation_shouldFailWhenDeliveryAddressIsNull() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "large", 1, new BigDecimal("10.00")))
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("deliveryAddress")));
    }

    @Test
    void validation_shouldFailWhenItemsListIsNull() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                null
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));
    }

    @Test
    void validation_shouldFailWhenItemsListIsEmpty() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                Collections.emptyList()
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));
    }

    @Test
    void validation_shouldFailWhenDeliveryAddressIsInvalid() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "large", 1, new BigDecimal("10.00")))
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("deliveryAddress")));
    }

    @Test
    void validation_shouldFailWhenItemIsInvalid() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(null, "Pizza", "spicy", 1, new BigDecimal("10.00")))
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("items")));
    }

    @Test
    void validation_shouldPassWithMultipleItems() {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(
                        new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "large", 1, new BigDecimal("10.00")),
                        new CustomerOrderItemRequest(UUID.randomUUID(), "Pasta", "small", 2, new BigDecimal("8.00"))
                )
        );

        Set<ConstraintViolation<CreateCustomerOrderRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
