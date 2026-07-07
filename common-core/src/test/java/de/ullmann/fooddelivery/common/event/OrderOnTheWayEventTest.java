package de.ullmann.fooddelivery.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderOnTheWayEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime pickedUpAt = LocalDateTime.now();

    @Test
    void shouldCreateOrderOnTheWayEventWithValidData() {
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);

        assertEquals(orderId, event.orderId());
        assertEquals(pickedUpAt, event.pickedUpAt());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenOrderIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                        new OrderOnTheWayEvent(null, customerId, pickedUpAt),
                "orderId must not be null"
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenPickedUpAtIsNull() {
        assertThrows(NullPointerException.class, () ->
                        new OrderOnTheWayEvent(orderId, customerId, null),
                "pickedUpAt must not be null"
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenBothFieldsAreNull() {
        assertThrows(NullPointerException.class, () ->
                        new OrderOnTheWayEvent(null, customerId, null),
                "orderId must not be null"
        );
    }

    @Test
    void shouldReturnSameHashCodeForEqualEvents() {
        OrderOnTheWayEvent event1 = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);
        OrderOnTheWayEvent event2 = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);

        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void shouldBeEqualForEventsWithSameData() {
        OrderOnTheWayEvent event1 = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);
        OrderOnTheWayEvent event2 = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);

        assertEquals(event1, event2);
    }

    @Test
    void shouldNotBeEqualForEventsWithDifferentOrderIds() {
        OrderOnTheWayEvent event1 = new OrderOnTheWayEvent(UUID.randomUUID(), customerId, pickedUpAt);
        OrderOnTheWayEvent event2 = new OrderOnTheWayEvent(UUID.randomUUID(), customerId, pickedUpAt);

        assertNotEquals(event1, event2);
    }

    @Test
    void shouldNotBeEqualForEventsWithDifferentPickedUpAt() {
        OrderOnTheWayEvent event1 = new OrderOnTheWayEvent(orderId, customerId,
                LocalDateTime.of(2026, 3, 30, 10, 30, 45));
        OrderOnTheWayEvent event2 = new OrderOnTheWayEvent(orderId, customerId,
                LocalDateTime.of(2026, 3, 30, 11, 30, 45));

        assertNotEquals(event1, event2);
    }

    @Test
    void shouldGenerateToStringRepresentation() {
        LocalDateTime pickedUpAt = LocalDateTime.of(2026, 3, 30, 10, 30, 45);

        OrderOnTheWayEvent event = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);
        String toStringResult = event.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("OrderOnTheWayEvent"));
        assertTrue(toStringResult.contains(orderId.toString()));
        assertTrue(toStringResult.contains("2026-03-30"));
    }

    @Test
    void recordShouldBecomeCanonicalAfterCreation() {
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(orderId, customerId, pickedUpAt);

        assertNotNull(event);
        assertInstanceOf(OrderOnTheWayEvent.class, event);
    }
}
