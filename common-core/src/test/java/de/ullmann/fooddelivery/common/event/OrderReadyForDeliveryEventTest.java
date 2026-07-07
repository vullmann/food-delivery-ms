package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

class OrderReadyForDeliveryEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final Address pickupAddress = Address.of("Restaurant St", "1", "Berlin", "10115", "DE");
    private final Address deliveryAddress = Address.of("Musterstraße 1", "23", "Halle", "06108", "DE");
    private final UUID customerId = UUID.randomUUID();
    private final LocalDateTime readyAt = LocalDateTime.now();

    @Test
    void shouldCreateEventWithValidData() {
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId,
                pickupAddress, deliveryAddress, readyAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.restaurantId()).isEqualTo(restaurantId);
        assertThat(event.pickupAddress()).isEqualTo(pickupAddress);
        assertThat(event.deliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(event.readyAt()).isEqualTo(readyAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderReadyForDeliveryEvent(null, customerId, restaurantId, pickupAddress,
                        deliveryAddress, readyAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenRestaurantIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderReadyForDeliveryEvent(orderId, customerId, null, pickupAddress,
                        deliveryAddress, readyAt))
                .withMessage("restaurantId must not be null");
    }

    @Test
    void shouldThrowWhenPickupAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId, null,
                        deliveryAddress, readyAt))
                .withMessage("pickupAddress must not be null");
    }

    @Test
    void shouldThrowWhenDeliveryAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId, pickupAddress,
                                null, readyAt))
                .withMessage("deliveryAddress must not be null");
    }

    @Test
    void shouldThrowWhenReadyAtIsNull() {
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId, pickupAddress,
                                deliveryAddress, null))
                .withMessage("readyAt must not be null");
    }

    @Test
    void shouldSupportRecordEquality() {
        OrderReadyForDeliveryEvent event1 = new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId,
                pickupAddress, deliveryAddress, readyAt);
        OrderReadyForDeliveryEvent event2 = new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId,
                pickupAddress, deliveryAddress, readyAt);

        assertThat(event1).isEqualTo(event2).hasSameHashCodeAs(event2);
    }

    @Test
    void shouldSupportRecordToString() {
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId,
                pickupAddress, deliveryAddress, readyAt);

        assertThat(event.toString())
                .contains("OrderReadyForDeliveryEvent")
                .contains(orderId.toString());
    }
}
