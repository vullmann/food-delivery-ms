package de.ullmann.fooddelivery.deliverservice.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

class DeliveryOrderTest {

    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private Address pickupAddress;
    private Address deliveryAddress;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        pickupAddress = Address.of("Restaurant St", "5", "Berlin", "10119", "Germany");
        deliveryAddress = Address.of("Main St", "1", "Berlin", "10115", "Germany");
    }

    @Test
    void create_shouldInitializeWithPendingStatus() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);

        assertThat(delivery.getId()).isNotNull();
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
        assertThat(delivery.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getDriverId()).isNull();
    }

    @Test
    void assignDriver_shouldSetDriverAndTransitionToDriverAssigned() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        UUID driverId = UUID.randomUUID();

        delivery.assignDriver(driverId);

        assertThat(delivery.getDriverId()).isEqualTo(driverId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DRIVER_ASSIGNED);
    }

    @Test
    void transitionTo_pickedUp_fromDriverAssigned_shouldSucceed() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.assignDriver(UUID.randomUUID());

        delivery.transitionTo(DeliveryStatus.PICKED_UP);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);
    }

    @Test
    void transitionTo_delivered_fromPickedUp_shouldSucceed() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.assignDriver(UUID.randomUUID());
        delivery.transitionTo(DeliveryStatus.PICKED_UP);

        delivery.transitionTo(DeliveryStatus.DELIVERED);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void transitionTo_cancelled_fromPending_shouldSucceed() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);

        delivery.transitionTo(DeliveryStatus.CANCELLED);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    }

    @Test
    void transitionTo_shouldThrow_whenSkippingDriverAssigned() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);

        assertThatThrownBy(() -> delivery.transitionTo(DeliveryStatus.PICKED_UP))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("PICKED_UP");
    }

    @Test
    void transitionTo_shouldThrow_whenDeliveredIsTerminal() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.assignDriver(UUID.randomUUID());
        delivery.transitionTo(DeliveryStatus.PICKED_UP);
        delivery.transitionTo(DeliveryStatus.DELIVERED);

        assertThatThrownBy(() -> delivery.transitionTo(DeliveryStatus.CANCELLED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    void transitionTo_shouldThrow_whenCancelledIsTerminal() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.transitionTo(DeliveryStatus.CANCELLED);

        assertThatThrownBy(() -> delivery.transitionTo(DeliveryStatus.DRIVER_ASSIGNED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CANCELLED");
    }
}
