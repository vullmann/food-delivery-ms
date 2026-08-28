package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

class CustomerCreatedEventTest {

    private final UUID customerId = UUID.randomUUID();
    private final String firstName = "Max";
    private final String lastName = "Mustermann";
    private final String email = "max.mustermann@example.com";
    private final String phone = "+49123456789";
    private final Address address = Address.of("Musterstraße 1", "23", "Halle", "06108", "DE");
    private final LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    @Test
    void shouldCreateEventWithValidData() {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                customerId, firstName, lastName, email, phone, address, createdAt
        );

        assertThat(event.customerId()).isEqualTo(customerId);
        assertThat(event.firstName()).isEqualTo(firstName);
        assertThat(event.lastName()).isEqualTo(lastName);
        assertThat(event.email()).isEqualTo(email);
        assertThat(event.phone()).isEqualTo(phone);
        assertThat(event.address()).isEqualTo(address);
        assertThat(event.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        null, firstName, lastName, email, phone, address, createdAt))
                .withMessage("customerId must not be null");
    }

    @Test
    void shouldThrowWhenFirstNameIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        customerId, null, lastName, email, phone, address, createdAt))
                .withMessage("firstName must not be null");
    }

    @Test
    void shouldThrowWhenLastNameIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        customerId, firstName, null, email, phone, address, createdAt))
                .withMessage("lastName must not be null");
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        customerId, firstName, lastName, null, phone, address, createdAt))
                .withMessage("email must not be null");
    }

    @Test
    void shouldThrowWhenPhoneIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        customerId, firstName, lastName, email, null, address, createdAt))
                .withMessage("phone must not be null");
    }

    @Test
    void shouldThrowWhenAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        customerId, firstName, lastName, email, phone, null, createdAt))
                .withMessage("address must not be null");
    }

    @Test
    void shouldThrowWhenCreatedAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CustomerCreatedEvent(
                        customerId, firstName, lastName, email, phone, address, null))
                .withMessage("createdAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        CustomerCreatedEvent event1 = new CustomerCreatedEvent(
                customerId, firstName, lastName, email, phone, address, createdAt);

        CustomerCreatedEvent event2 = new CustomerCreatedEvent(
                customerId, firstName, lastName, email, phone, address, createdAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                customerId, firstName, lastName, email, phone, address, createdAt);

        assertThat(event.toString())
                .contains("CustomerCreatedEvent")
                .contains(customerId.toString())
                .contains(firstName);
    }
}