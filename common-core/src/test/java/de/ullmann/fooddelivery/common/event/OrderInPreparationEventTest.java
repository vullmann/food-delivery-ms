package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderInPreparationEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime startedAt = LocalDateTime.now(ZoneOffset.UTC);

    @Test
    void shouldCreateEventWithValidData() {
        OrderInPreparationEvent event = new OrderInPreparationEvent(orderId, customerId, startedAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.startedAt()).isEqualTo(startedAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderInPreparationEvent(null, customerId, startedAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenCompletedAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderInPreparationEvent(orderId, customerId, null))
                .withMessage("startedAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        OrderInPreparationEvent event1 = new OrderInPreparationEvent(orderId, customerId, startedAt);
        OrderInPreparationEvent event2 = new OrderInPreparationEvent(orderId, customerId, startedAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        OrderInPreparationEvent event = new OrderInPreparationEvent(orderId, customerId, startedAt);

        assertThat(event.toString())
                .contains("OrderInPreparationEvent")
                .contains(orderId.toString());
    }
}
