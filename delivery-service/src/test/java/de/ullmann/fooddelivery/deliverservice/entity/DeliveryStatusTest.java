package de.ullmann.fooddelivery.deliverservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryStatusTest {

    @Test
    void pending_canTransitionToDriverAssigned() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.DRIVER_ASSIGNED)).isTrue();
    }

    @Test
    void pending_canTransitionToCancelled() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.CANCELLED)).isTrue();
    }

    @Test
    void pending_cannotTransitionToPickedUp() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.PICKED_UP)).isFalse();
    }

    @Test
    void pending_cannotTransitionToDelivered() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.DELIVERED)).isFalse();
    }

    @Test
    void driverAssigned_canTransitionToPickedUp() {
        assertThat(DeliveryStatus.DRIVER_ASSIGNED.canTransitionTo(DeliveryStatus.PICKED_UP)).isTrue();
    }

    @Test
    void driverAssigned_canTransitionToCancelled() {
        assertThat(DeliveryStatus.DRIVER_ASSIGNED.canTransitionTo(DeliveryStatus.CANCELLED)).isTrue();
    }

    @Test
    void driverAssigned_cannotTransitionToDelivered() {
        assertThat(DeliveryStatus.DRIVER_ASSIGNED.canTransitionTo(DeliveryStatus.DELIVERED)).isFalse();
    }

    @Test
    void pickedUp_canTransitionToDelivered() {
        assertThat(DeliveryStatus.PICKED_UP.canTransitionTo(DeliveryStatus.DELIVERED)).isTrue();
    }

    @Test
    void pickedUp_canTransitionToCancelled() {
        assertThat(DeliveryStatus.PICKED_UP.canTransitionTo(DeliveryStatus.CANCELLED)).isTrue();
    }

    @Test
    void pickedUp_cannotTransitionToDriverAssigned() {
        assertThat(DeliveryStatus.PICKED_UP.canTransitionTo(DeliveryStatus.DRIVER_ASSIGNED)).isFalse();
    }

    @Test
    void delivered_cannotTransitionToAnything() {
        for (DeliveryStatus target : DeliveryStatus.values()) {
            assertThat(DeliveryStatus.DELIVERED.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    void cancelled_cannotTransitionToAnything() {
        for (DeliveryStatus target : DeliveryStatus.values()) {
            assertThat(DeliveryStatus.CANCELLED.canTransitionTo(target)).isFalse();
        }
    }
}
