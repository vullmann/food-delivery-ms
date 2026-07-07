package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderDeliveredEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime deliveredAt = LocalDateTime.now();

    @Test
    void shouldCreateEventWithValidData() {
        OrderDeliveredEvent event = new OrderDeliveredEvent(orderId, customerId, deliveredAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.deliveredAt()).isEqualTo(deliveredAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderDeliveredEvent(null, customerId, deliveredAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenDeliveredAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderDeliveredEvent(orderId, customerId, null))
                .withMessage("deliveredAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        OrderDeliveredEvent event1 = new OrderDeliveredEvent(orderId, customerId, deliveredAt);
        OrderDeliveredEvent event2 = new OrderDeliveredEvent(orderId, customerId, deliveredAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        OrderDeliveredEvent event = new OrderDeliveredEvent(orderId, customerId, deliveredAt);

        assertThat(event.toString())
                .contains("OrderDeliveredEvent")
                .contains(orderId.toString());
    }
}
