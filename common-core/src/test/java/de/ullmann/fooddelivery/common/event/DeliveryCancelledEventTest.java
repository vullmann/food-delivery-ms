package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DeliveryCancelledEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime cancelledAt = LocalDateTime.now(ZoneOffset.UTC);

    @Test
    void shouldCreateEventWithValidData() {
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(orderId, customerId, cancelledAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.cancelledAt()).isEqualTo(cancelledAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DeliveryCancelledEvent(null, customerId, cancelledAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenCancelledAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DeliveryCancelledEvent(orderId, customerId, null))
                .withMessage("cancelledAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        DeliveryCancelledEvent event1 = new DeliveryCancelledEvent(orderId, customerId, cancelledAt);
        DeliveryCancelledEvent event2 = new DeliveryCancelledEvent(orderId, customerId, cancelledAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(orderId, customerId, cancelledAt);

        assertThat(event.toString())
                .contains("DeliveryCancelledEvent")
                .contains(orderId.toString());
    }
}
