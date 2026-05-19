package de.ullmann.fooddelivery.orderservice.kafka;

import static org.mockito.Mockito.verify;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.service.CustomerOrderService;

@ExtendWith(MockitoExtension.class)
class CustomerOrderEventConsumerTest {

    @Mock
    private CustomerOrderService customerOrderService;

    @InjectMocks
    private CustomerOrderEventConsumer consumer;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();

    private static final Address PICKUP_ADDRESS = Address.of("Friedrichstraße", "42", "Berlin", "'10117", "Germany");
    private static final Address DELIVERY_ADDRESS = Address.of("Main St", "1", "Berlin", "10115", "Germany");

    @Test
    void onOrderConfirmed_shouldUpdateStatusToConfirmed() {
        OrderConfirmedEvent event = new OrderConfirmedEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());
        consumer.onOrderConfirmed(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.CONFIRMED);
    }

    @Test
    void onOrderInPreparation_shouldUpdateStatusToPreparing() {
        OrderInPreparationEvent event = new OrderInPreparationEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());
        consumer.onOrderInPreparation(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.PREPARING);
    }

    @Test
    void onOrderReadyForDelivery_shouldUpdateStatusToReadyForDelivery() {
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(ORDER_ID, CUSTOMER_ID, RESTAURANT_ID,
                PICKUP_ADDRESS, DELIVERY_ADDRESS, LocalDateTime.now());
        consumer.onOrderReadyForDelivery(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    void onDriverAssigned_shouldUpdateStatusToDriverAssigned() {
        DriverAssignedEvent event = new DriverAssignedEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());
        consumer.onDriverAssigned(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.DRIVER_ASSIGNED);
    }

    @Test
    void onOrderOnTheWay_shouldUpdateStatusToOnTheWay() {
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());
        consumer.onOrderOnTheWay(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.ON_THE_WAY);
    }

    @Test
    void onOrderDelivered_shouldUpdateStatusToDelivered() {
        OrderDeliveredEvent event = new OrderDeliveredEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());
        consumer.onOrderDelivered(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.DELIVERED);
    }

    @Test
    void onRestaurantOrderCancelled_shouldUpdateStatusToCancelled() {
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(ORDER_ID, CUSTOMER_ID,
                LocalDateTime.now());
        consumer.onRestaurantOrderCancelled(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.CANCELLED);
    }

    @Test
    void onDeliveryCancelled_shouldUpdateStatusToCancelled() {
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());
        consumer.onDeliveryCancelled(event);
        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.CANCELLED);
    }
}
