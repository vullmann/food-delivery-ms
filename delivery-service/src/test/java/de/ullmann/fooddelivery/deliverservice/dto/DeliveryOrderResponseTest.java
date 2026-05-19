package de.ullmann.fooddelivery.deliverservice.dto;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryOrderResponseTest {

    @Test
    void from_shouldMapAllFields() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Address pickup = Address.of("Main St", "1", "Berlin", "10115", "Germany");
        Address delivery = Address.of("Other St", "2", "Berlin", "10116", "Germany");

        DeliveryOrder order = DeliveryOrder.create(orderId, customerId, restaurantId, pickup, delivery);
        DeliveryOrderResponse response = DeliveryOrderResponse.from(order);

        assertThat(response.id()).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.restaurantId()).isEqualTo(restaurantId);
        assertThat(response.driverId()).isNull();
        assertThat(response.pickupAddress()).isEqualTo(pickup);
        assertThat(response.deliveryAddress()).isEqualTo(delivery);
        assertThat(response.status()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void directConstruction_shouldWork() {
        UUID id = UUID.randomUUID();
        DeliveryOrderResponse response = new DeliveryOrderResponse(id, null, null, null, null, null, DeliveryStatus.PENDING, null, null);
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.status()).isEqualTo(DeliveryStatus.PENDING);
    }
}
