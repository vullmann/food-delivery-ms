package de.ullmann.fooddelivery.restaurantservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantOrderStatusTest {

    @Test
    void received_canTransitionToConfirmed() {
        assertThat(RestaurantOrderStatus.RECEIVED.canTransitionTo(RestaurantOrderStatus.CONFIRMED)).isTrue();
    }

    @Test
    void received_canTransitionToCancelled() {
        assertThat(RestaurantOrderStatus.RECEIVED.canTransitionTo(RestaurantOrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void received_cannotTransitionToPreparing() {
        assertThat(RestaurantOrderStatus.RECEIVED.canTransitionTo(RestaurantOrderStatus.PREPARING)).isFalse();
    }

    @Test
    void confirmed_canTransitionToPreparing() {
        assertThat(RestaurantOrderStatus.CONFIRMED.canTransitionTo(RestaurantOrderStatus.PREPARING)).isTrue();
    }

    @Test
    void confirmed_canTransitionToCancelled() {
        assertThat(RestaurantOrderStatus.CONFIRMED.canTransitionTo(RestaurantOrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void confirmed_cannotTransitionToReadyForDelivery() {
        assertThat(RestaurantOrderStatus.CONFIRMED.canTransitionTo(RestaurantOrderStatus.READY_FOR_DELIVERY)).isFalse();
    }

    @Test
    void preparing_canTransitionToReadyForDelivery() {
        assertThat(RestaurantOrderStatus.PREPARING.canTransitionTo(RestaurantOrderStatus.READY_FOR_DELIVERY)).isTrue();
    }

    @Test
    void preparing_canTransitionToCancelled() {
        assertThat(RestaurantOrderStatus.PREPARING.canTransitionTo(RestaurantOrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void preparing_cannotTransitionToReceived() {
        assertThat(RestaurantOrderStatus.PREPARING.canTransitionTo(RestaurantOrderStatus.RECEIVED)).isFalse();
    }

    @Test
    void readyForDelivery_canTransitionToPickedUp() {
        assertThat(RestaurantOrderStatus.READY_FOR_DELIVERY.canTransitionTo(RestaurantOrderStatus.PICKED_UP)).isTrue();
    }

    @Test
    void readyForDelivery_canTransitionToCancelled() {
        assertThat(RestaurantOrderStatus.READY_FOR_DELIVERY.canTransitionTo(RestaurantOrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void readyForDelivery_cannotTransitionToReceived() {
        assertThat(RestaurantOrderStatus.READY_FOR_DELIVERY.canTransitionTo(RestaurantOrderStatus.RECEIVED)).isFalse();
    }

    @Test
    void pickedUp_cannotTransitionToAnything() {
        for (RestaurantOrderStatus target : RestaurantOrderStatus.values()) {
            assertThat(RestaurantOrderStatus.PICKED_UP.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    void cancelled_cannotTransitionToAnything() {
        for (RestaurantOrderStatus target : RestaurantOrderStatus.values()) {
            assertThat(RestaurantOrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
        }
    }
}
