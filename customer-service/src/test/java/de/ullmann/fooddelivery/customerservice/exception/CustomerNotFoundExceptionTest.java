package de.ullmann.fooddelivery.customerservice.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithUUID() {
        UUID customerId = UUID.randomUUID();

        CustomerNotFoundException exception = new CustomerNotFoundException(customerId);

        assertNotNull(exception);
        assertEquals("Customer not found: " + customerId, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithEmail() {
        String email = "test@example.com";

        CustomerNotFoundException exception = new CustomerNotFoundException(email);

        assertNotNull(exception);
        assertEquals("Customer not found: " + email, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        UUID customerId = UUID.randomUUID();

        CustomerNotFoundException exception = new CustomerNotFoundException(customerId);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void shouldHandleDifferentUUIDs() {
        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();

        CustomerNotFoundException exception1 = new CustomerNotFoundException(customerId1);
        CustomerNotFoundException exception2 = new CustomerNotFoundException(customerId2);

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains(customerId1.toString()));
        assertTrue(exception2.getMessage().contains(customerId2.toString()));
    }

    @Test
    void shouldHandleDifferentEmails() {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        CustomerNotFoundException exception1 = new CustomerNotFoundException(email1);
        CustomerNotFoundException exception2 = new CustomerNotFoundException(email2);

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains(email1));
        assertTrue(exception2.getMessage().contains(email2));
    }
}
