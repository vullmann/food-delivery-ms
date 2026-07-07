package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RestaurantOrderCancelledEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime cancelledAt = LocalDateTime.now();

    @Test
    void shouldCreateEventWithValidData() {
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(orderId, customerId, cancelledAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.cancelledAt()).isEqualTo(cancelledAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new RestaurantOrderCancelledEvent(null, customerId, cancelledAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenCancelledAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new RestaurantOrderCancelledEvent(orderId, customerId, null))
                .withMessage("cancelledAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        RestaurantOrderCancelledEvent event1 = new RestaurantOrderCancelledEvent(orderId, customerId, cancelledAt);
        RestaurantOrderCancelledEvent event2 = new RestaurantOrderCancelledEvent(orderId, customerId, cancelledAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(orderId, customerId, cancelledAt);

        assertThat(event.toString())
                .contains("RestaurantOrderCancelledEvent")
                .contains(orderId.toString());
    }
}
