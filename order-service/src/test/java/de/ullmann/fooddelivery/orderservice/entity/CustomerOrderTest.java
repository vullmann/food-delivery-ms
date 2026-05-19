package de.ullmann.fooddelivery.orderservice.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

class CustomerOrderTest {

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------


    @Test
    void create_shouldCreateOrderWithCorrectValues() {
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");
        List<CustomerOrderItem> items = List.of(
                CustomerOrderItem.create(UUID.randomUUID(), "Pizza", "Classic pizza", 1, new BigDecimal("10.00")),
                CustomerOrderItem.create(UUID.randomUUID(), "Pasta", "Fresh pasta", 2, new BigDecimal("8.00"))
        );

        CustomerOrder customerOrder = CustomerOrder.create(customerId, restaurantId, address, items);

        assertThat(customerOrder).isNotNull();
        assertThat(customerOrder.getId()).isNotNull();
        assertThat(customerOrder.getCreatedAt()).isNotNull();
        assertThat(customerOrder.getCustomerId()).isEqualTo(customerId);
        assertThat(customerOrder.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(customerOrder.getDeliveryAddress()).isEqualTo(address);
        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CREATED);
        assertThat(customerOrder.getItems()).hasSize(2);
    }

    @Test
    void create_shouldCalculateTotalAmountCorrectly() {
        List<CustomerOrderItem> items = List.of(
                CustomerOrderItem.create(UUID.randomUUID(), "Item1", "First item", 2, new BigDecimal("10.00")),
                CustomerOrderItem.create(UUID.randomUUID(), "Item2", "Second item", 1, new BigDecimal("15.00"))
        );

        CustomerOrder customerOrder = CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                items
        );

        assertThat(customerOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    void create_shouldAssignOrderReferenceToAllItems() {
        List<CustomerOrderItem> items = List.of(
                CustomerOrderItem.create(UUID.randomUUID(), "Item1", "First item", 1, new BigDecimal("10.00"))
        );

        CustomerOrder customerOrder = CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                items
        );

        customerOrder.getItems().forEach(item -> {
            assertThat(item.getCustomerOrder()).isNotNull();
            assertThat(item.getCustomerOrder()).isEqualTo(customerOrder);
        });
    }

    // -------------------------------------------------------------------------
    // transitionTo() – valid transitions
    // -------------------------------------------------------------------------

    @Test
    void transitionTo_shouldAllowPendingToConfirmed() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CONFIRMED);
    }

    @Test
    void transitionTo_shouldAllowConfirmedToPreparing() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);

        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.PREPARING);
    }

    @Test
    void transitionTo_shouldAllowConfirmedToCancelled() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);

        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
    }

    @Test
    void transitionTo_shouldAllowPreparingToReadyForDelivery() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);

        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    void transitionTo_shouldAllowPreparingToCancelled() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);

        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
    }

    @Test
    void transitionTo_shouldAllowReadyForDeliveryToDriverAssigned() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);

        customerOrder.transitionTo(CustomerOrderStatus.DRIVER_ASSIGNED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.DRIVER_ASSIGNED);
    }

    @Test
    void transitionTo_shouldAllowDriverAssignedToOnTheWay() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);
        customerOrder.transitionTo(CustomerOrderStatus.DRIVER_ASSIGNED);

        customerOrder.transitionTo(CustomerOrderStatus.ON_THE_WAY);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.ON_THE_WAY);
    }

    @Test
    void transitionTo_shouldAllowOnTheWayToDelivered() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);
        customerOrder.transitionTo(CustomerOrderStatus.DRIVER_ASSIGNED);
        customerOrder.transitionTo(CustomerOrderStatus.ON_THE_WAY);

        customerOrder.transitionTo(CustomerOrderStatus.DELIVERED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.DELIVERED);
    }

    // -------------------------------------------------------------------------
    // transitionTo() – invalid transitions
    // -------------------------------------------------------------------------

    @Test
    void transitionTo_shouldThrowForPendingToDelivered() {
        CustomerOrder customerOrder = createTestOrder();

        assertThatThrownBy(() -> customerOrder.transitionTo(CustomerOrderStatus.DELIVERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from CREATED to DELIVERED");
    }

    @Test
    void transitionTo_shouldThrowForDeliveredToAnything() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);
        customerOrder.transitionTo(CustomerOrderStatus.DRIVER_ASSIGNED);
        customerOrder.transitionTo(CustomerOrderStatus.ON_THE_WAY);
        customerOrder.transitionTo(CustomerOrderStatus.DELIVERED);

        assertThatThrownBy(() -> customerOrder.transitionTo(CustomerOrderStatus.CANCELLED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from DELIVERED to CANCELLED");
    }

    @Test
    void transitionTo_shouldThrowForCancelledToAnything() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThatThrownBy(() -> customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from CANCELLED to CONFIRMED");
    }

    // -------------------------------------------------------------------------
    // create() – guard clauses
    // -------------------------------------------------------------------------

    @Test
    void create_shouldThrow_whenItemsIsNull() {
        assertThatThrownBy(() -> CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    void create_shouldThrow_whenItemsIsEmpty() {
        assertThatThrownBy(() -> CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must contain at least one item");
    }

    // -------------------------------------------------------------------------
    // transitionTo() – missing CANCELLED paths
    // -------------------------------------------------------------------------

    @Test
    void transitionTo_shouldAllowPendingToCancelled() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);

        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
    }

    @Test
    void transitionTo_shouldAllowReadyForDeliveryToCancelled() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);

        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
    }

    @Test
    void transitionTo_shouldAllowDriverAssignedToCancelled() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);
        customerOrder.transitionTo(CustomerOrderStatus.DRIVER_ASSIGNED);

        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
    }

    @Test
    void transitionTo_shouldAllowOnTheWayToCancelled() {
        CustomerOrder customerOrder = createTestOrder();
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);
        customerOrder.transitionTo(CustomerOrderStatus.PREPARING);
        customerOrder.transitionTo(CustomerOrderStatus.READY_FOR_DELIVERY);
        customerOrder.transitionTo(CustomerOrderStatus.DRIVER_ASSIGNED);
        customerOrder.transitionTo(CustomerOrderStatus.ON_THE_WAY);

        customerOrder.transitionTo(CustomerOrderStatus.CANCELLED);

        assertThat(customerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private CustomerOrder createTestOrder() {
        return CustomerOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("St", "1", "City", "12345", "Country"),
                List.of(CustomerOrderItem.create(UUID.randomUUID(), "Test Item", "Test item description", 1,
                        new BigDecimal("10.00")))
        );
    }
}