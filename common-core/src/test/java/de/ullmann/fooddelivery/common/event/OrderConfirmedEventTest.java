package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderConfirmedEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime confirmedAt = LocalDateTime.now(ZoneOffset.UTC);

    @Test
    void shouldCreateEventWithValidData() {
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderId, customerId, confirmedAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.confirmedAt()).isEqualTo(confirmedAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderConfirmedEvent(null, customerId, confirmedAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenConfirmedAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderConfirmedEvent(orderId, customerId, null))
                .withMessage("confirmedAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        OrderConfirmedEvent event1 = new OrderConfirmedEvent(orderId, customerId, confirmedAt);
        OrderConfirmedEvent event2 = new OrderConfirmedEvent(orderId, customerId, confirmedAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderId, customerId, confirmedAt);

        assertThat(event.toString())
                .contains("OrderConfirmedEvent")
                .contains(orderId.toString());
    }
}
