package de.ullmann.fooddelivery.orderservice.sqs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

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
class SqsOrderEventConsumerTest {

    @Mock
    private CustomerOrderService customerOrderService;

    private ObjectMapper objectMapper;
    private SqsOrderEventConsumer consumer;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final Address ADDRESS = Address.of("Main St", "1", "Berlin", "10115", "Germany");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new SqsOrderEventConsumer(customerOrderService, objectMapper);
    }

    @Test
    void onOrderConfirmed_shouldUpdateStatusToConfirmed() throws Exception {
        OrderConfirmedEvent event = new OrderConfirmedEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderConfirmed(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.CONFIRMED);
    }

    @Test
    void onOrderConfirmed_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderConfirmed("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onOrderInPreparation_shouldUpdateStatusToPreparing() throws Exception {
        OrderInPreparationEvent event = new OrderInPreparationEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderInPreparation(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.PREPARING);
    }

    @Test
    void onOrderInPreparation_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderInPreparation("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onOrderReadyForDelivery_shouldUpdateStatusToReadyForDelivery() throws Exception {
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(
                ORDER_ID, CUSTOMER_ID, RESTAURANT_ID, ADDRESS, ADDRESS, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderReadyForDelivery(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    void onOrderReadyForDelivery_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderReadyForDelivery("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onDriverAssigned_shouldUpdateStatusToDriverAssigned() throws Exception {
        DriverAssignedEvent event = new DriverAssignedEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onDriverAssigned(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.DRIVER_ASSIGNED);
    }

    @Test
    void onDriverAssigned_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onDriverAssigned("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onOrderOnTheWay_shouldUpdateStatusToOnTheWay() throws Exception {
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderOnTheWay(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.ON_THE_WAY);
    }

    @Test
    void onOrderOnTheWay_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderOnTheWay("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onOrderDelivered_shouldUpdateStatusToDelivered() throws Exception {
        OrderDeliveredEvent event = new OrderDeliveredEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderDelivered(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.DELIVERED);
    }

    @Test
    void onOrderDelivered_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderDelivered("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onRestaurantOrderCancelled_shouldUpdateStatusToCancelled() throws Exception {
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onRestaurantOrderCancelled(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.CANCELLED);
    }

    @Test
    void onRestaurantOrderCancelled_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onRestaurantOrderCancelled("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void onDeliveryCancelled_shouldUpdateStatusToCancelled() throws Exception {
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onDeliveryCancelled(objectMapper.writeValueAsString(event));

        verify(customerOrderService).updateStatus(ORDER_ID, CustomerOrderStatus.CANCELLED);
    }

    @Test
    void onDeliveryCancelled_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onDeliveryCancelled("not-valid-json");

        verifyNoInteractions(customerOrderService);
    }
}
