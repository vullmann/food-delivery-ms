package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderCancelledEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final LocalDateTime cancelledAt = LocalDateTime.now();

    @Test
    void shouldCreateEventWithValidData() {
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, cancelledAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.cancelledAt()).isEqualTo(cancelledAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderCancelledEvent(null, cancelledAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenCancelledAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderCancelledEvent(orderId, null))
                .withMessage("cancelledAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        OrderCancelledEvent event1 = new OrderCancelledEvent(orderId, cancelledAt);
        OrderCancelledEvent event2 = new OrderCancelledEvent(orderId, cancelledAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, cancelledAt);

        assertThat(event.toString())
                .contains("OrderCancelledEvent")
                .contains(orderId.toString());
    }
}