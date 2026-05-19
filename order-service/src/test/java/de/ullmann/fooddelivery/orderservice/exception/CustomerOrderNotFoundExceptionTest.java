package de.ullmann.fooddelivery.orderservice.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerOrderNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithCorrectMessage() {
        UUID orderId = UUID.randomUUID();

        CustomerOrderNotFoundException exception = new CustomerOrderNotFoundException(orderId);

        assertNotNull(exception);
        assertEquals("Order not found: " + orderId, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        UUID orderId = UUID.randomUUID();

        CustomerOrderNotFoundException exception = new CustomerOrderNotFoundException(orderId);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void shouldHandleDifferentUUIDs() {
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        CustomerOrderNotFoundException exception1 = new CustomerOrderNotFoundException(orderId1);
        CustomerOrderNotFoundException exception2 = new CustomerOrderNotFoundException(orderId2);

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains(orderId1.toString()));
        assertTrue(exception2.getMessage().contains(orderId2.toString()));
    }
}
