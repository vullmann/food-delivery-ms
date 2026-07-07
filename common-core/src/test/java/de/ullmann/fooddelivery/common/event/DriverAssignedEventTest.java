package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DriverAssignedEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime assignedAt = LocalDateTime.now();

    @Test
    void shouldCreateEventWithValidData() {
        DriverAssignedEvent event = new DriverAssignedEvent(orderId, customerId, assignedAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.assignedAt()).isEqualTo(assignedAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DriverAssignedEvent(null, customerId, assignedAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenCancelledAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DriverAssignedEvent(orderId, customerId, null))
                .withMessage("assignedAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        DriverAssignedEvent event1 = new DriverAssignedEvent(orderId, customerId, assignedAt);
        DriverAssignedEvent event2 = new DriverAssignedEvent(orderId, customerId, assignedAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        DriverAssignedEvent event = new DriverAssignedEvent(orderId, customerId, assignedAt);

        assertThat(event.toString())
                .contains("DriverAssignedEvent")
                .contains(orderId.toString());
    }
}
