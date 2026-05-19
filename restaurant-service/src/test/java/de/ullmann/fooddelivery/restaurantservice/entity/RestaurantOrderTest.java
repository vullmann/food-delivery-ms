package de.ullmann.fooddelivery.restaurantservice.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestaurantOrderTest {

    private static final Address ADDRESS = Address.of("Main St", "1", "Berlin", "10115", "Germany");

    @Test
    void create_shouldSetInitialStatusToReceived() {
        UUID orderOrderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        RestaurantOrder order = RestaurantOrder.create(orderOrderId, restaurantId, customerId, ADDRESS);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerOrderId()).isEqualTo(orderOrderId);
        assertThat(order.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.RECEIVED);
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    void addItem_shouldAddItemToList() {
        RestaurantOrder order = RestaurantOrder.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ADDRESS);
        RestaurantOrderItem item = RestaurantOrderItem.create(UUID.randomUUID(), "Pizza", 2, BigDecimal.valueOf(8.00));

        order.addItem(item);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getName()).isEqualTo("Pizza");
    }

    @Test
    void transitionTo_shouldUpdateStatus() {
        RestaurantOrder order = RestaurantOrder.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ADDRESS);

        order.transitionTo(RestaurantOrderStatus.CONFIRMED);

        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.CONFIRMED);
    }

    @Test
    void transitionTo_invalidTransition_shouldThrowIllegalStateException() {
        RestaurantOrder order = RestaurantOrder.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ADDRESS);

        assertThatThrownBy(() -> order.transitionTo(RestaurantOrderStatus.PICKED_UP))
                .isInstanceOf(IllegalStateException.class);
    }
}
