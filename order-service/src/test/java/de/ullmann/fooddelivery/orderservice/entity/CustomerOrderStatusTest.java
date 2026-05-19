package de.ullmann.fooddelivery.orderservice.entity;

import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.CANCELLED;
import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.CONFIRMED;
import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.DELIVERED;
import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.DRIVER_ASSIGNED;
import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.ON_THE_WAY;
import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.PREPARING;
import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.READY_FOR_DELIVERY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CustomerOrderStatusTest {

    @Test
    void shouldHaveAllExpectedStatuses() {
        CustomerOrderStatus[] statuses = CustomerOrderStatus.values();

        assertEquals(9, statuses.length);
        assertEquals(CustomerOrderStatus.CREATED, statuses[0]);
        assertEquals(CustomerOrderStatus.PENDING, statuses[1]);
        assertEquals(CustomerOrderStatus.CONFIRMED, statuses[2]);
        assertEquals(CustomerOrderStatus.PREPARING, statuses[3]);
        assertEquals(CustomerOrderStatus.READY_FOR_DELIVERY, statuses[4]);
        assertEquals(CustomerOrderStatus.DRIVER_ASSIGNED, statuses[5]);
        assertEquals(CustomerOrderStatus.ON_THE_WAY, statuses[6]);
        assertEquals(CustomerOrderStatus.DELIVERED, statuses[7]);
        assertEquals(CustomerOrderStatus.CANCELLED, statuses[8]);
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertEquals(CustomerOrderStatus.CREATED, CustomerOrderStatus.valueOf("CREATED"));
        assertEquals(CustomerOrderStatus.PENDING, CustomerOrderStatus.valueOf("PENDING"));
        assertEquals(CustomerOrderStatus.CONFIRMED, CustomerOrderStatus.valueOf("CONFIRMED"));
        assertEquals(CustomerOrderStatus.PREPARING, CustomerOrderStatus.valueOf("PREPARING"));
        assertEquals(CustomerOrderStatus.READY_FOR_DELIVERY, CustomerOrderStatus.valueOf("READY_FOR_DELIVERY"));
        assertEquals(CustomerOrderStatus.DRIVER_ASSIGNED, CustomerOrderStatus.valueOf("DRIVER_ASSIGNED"));
        assertEquals(CustomerOrderStatus.ON_THE_WAY, CustomerOrderStatus.valueOf("ON_THE_WAY"));
        assertEquals(CustomerOrderStatus.DELIVERED, CustomerOrderStatus.valueOf("DELIVERED"));
        assertEquals(CustomerOrderStatus.CANCELLED, CustomerOrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void valueOf_shouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> CustomerOrderStatus.valueOf("INVALID"));
    }

    // ── canTransitionTo – false branches not covered by CustomerOrderTest ──────

    @Test
    void confirmedCanTransitionTo_shouldReturnFalse_forInvalidTarget() {
        assertFalse(CONFIRMED.canTransitionTo(DELIVERED));
    }

    @Test
    void preparingCanTransitionTo_shouldReturnFalse_forInvalidTarget() {
        assertFalse(PREPARING.canTransitionTo(DELIVERED));
    }

    @Test
    void readyForDeliveryCanTransitionTo_shouldReturnFalse_forInvalidTarget() {
        assertFalse(READY_FOR_DELIVERY.canTransitionTo(CONFIRMED));
    }

    @Test
    void driverAssignedCanTransitionTo_shouldReturnFalse_forInvalidTarget() {
        assertFalse(DRIVER_ASSIGNED.canTransitionTo(CONFIRMED));
    }

    @Test
    void onTheWayCanTransitionTo_shouldReturnFalse_forInvalidTarget() {
        assertFalse(ON_THE_WAY.canTransitionTo(CONFIRMED));
    }
}
