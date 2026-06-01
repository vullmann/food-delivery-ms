package de.ullmann.fooddelivery.notificationservice.kafka;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderItemDto;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.notificationservice.projection.CustomerPhoneStore;
import de.ullmann.fooddelivery.notificationservice.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CustomerPhoneStore phoneStore;

    @InjectMocks
    private NotificationEventConsumer consumer;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final String PHONE = "+49123456789";
    private static final Address ADDRESS = Address.of("Main St", "1", "Berlin", "10115", "Germany");
    private static final Address PICKUP_ADDRESS = Address.of("Friedrichstraße", "42", "Berlin", "'10117", "Germany");
    private static final Address DELIVERY_ADDRESS = Address.of("Bornholmer Str.", "74", "Berlin", "10439", "Germany");


    @Test
    void onCustomerCreated_shouldUpsertAndSendWelcome() {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                CUSTOMER_ID, "John", "Doe", "john@doe.com", PHONE, ADDRESS, LocalDateTime.now());

        consumer.onCustomerCreated(event);

        verify(phoneStore).upsert(CUSTOMER_ID, PHONE);
        verify(notificationService).send(eq(PHONE), contains("Welcome"));
    }

    @Test
    void onCustomerProfileUpdated_shouldUpsertOnly() {
        CustomerProfileUpdatedEvent event = new CustomerProfileUpdatedEvent(CUSTOMER_ID, PHONE, LocalDateTime.now());

        consumer.onCustomerProfileUpdated(event);

        verify(phoneStore).upsert(CUSTOMER_ID, PHONE);
    }

    @Test
    void onOrderPlaced_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(), CUSTOMER_ID, UUID.randomUUID(), BigDecimal.valueOf(25.00),
                List.of(new OrderItemDto(UUID.randomUUID(), "Pizza", 1, BigDecimal.valueOf(25.00))),
                ADDRESS, LocalDateTime.now());

        consumer.onOrderPlaced(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderPlaced_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(), CUSTOMER_ID, UUID.randomUUID(), BigDecimal.valueOf(25.00),
                List.of(new OrderItemDto(UUID.randomUUID(), "Pizza", 1, BigDecimal.valueOf(25.00))),
                ADDRESS, LocalDateTime.now());

        consumer.onOrderPlaced(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderConfirmed_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderConfirmed(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderConfirmed_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderConfirmed(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderInPreparation_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderInPreparationEvent event = new OrderInPreparationEvent(UUID.randomUUID(), CUSTOMER_ID,
                LocalDateTime.now());

        consumer.onOrderInPreparation(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderInPreparation_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderInPreparationEvent event = new OrderInPreparationEvent(UUID.randomUUID(), CUSTOMER_ID,
                LocalDateTime.now());

        consumer.onOrderInPreparation(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderReadyForDelivery_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(UUID.randomUUID(), CUSTOMER_ID, RESTAURANT_ID,
                PICKUP_ADDRESS, DELIVERY_ADDRESS, LocalDateTime.now());

        consumer.onOrderReadyForDelivery(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderReadyForDelivery_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(UUID.randomUUID(), CUSTOMER_ID, RESTAURANT_ID,
                PICKUP_ADDRESS, DELIVERY_ADDRESS, LocalDateTime.now());

        consumer.onOrderReadyForDelivery(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderOnTheWay_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderOnTheWay(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderOnTheWay_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderOnTheWay(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderDelivered_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderDeliveredEvent event = new OrderDeliveredEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderDelivered(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderDelivered_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderDeliveredEvent event = new OrderDeliveredEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderDelivered(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onRestaurantOrderCancelled_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(UUID.randomUUID(), CUSTOMER_ID,
                LocalDateTime.now());

        consumer.onRestaurantOrderCancelled(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onRestaurantOrderCancelled_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(UUID.randomUUID(), CUSTOMER_ID,
                LocalDateTime.now());

        consumer.onRestaurantOrderCancelled(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onDeliveryCancelled_whenPhoneKnown_shouldSendNotification() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onDeliveryCancelled(event);

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onDeliveryCancelled_whenPhoneNull_shouldSkip() {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now());

        consumer.onDeliveryCancelled(event);

        verify(notificationService, never()).send(anyString(), anyString());
    }
}
