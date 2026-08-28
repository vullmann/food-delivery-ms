package de.ullmann.fooddelivery.notificationservice.sqs;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderItemDto;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.notificationservice.projection.CustomerPhoneStore;
import de.ullmann.fooddelivery.notificationservice.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class SqsNotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CustomerPhoneStore phoneStore;

    private ObjectMapper objectMapper;
    private SqsNotificationEventConsumer consumer;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String PHONE = "+49123456789";
    private static final Address ADDRESS = Address.of("Main St", "1", "Berlin", "10115", "Germany");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new SqsNotificationEventConsumer(notificationService, phoneStore, objectMapper);
    }

    // --- Projection maintenance ---

    @Test
    void onCustomerCreated_shouldUpsertPhoneAndSendWelcome() throws Exception {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                CUSTOMER_ID, "John", "Doe", "john@doe.com", PHONE, ADDRESS, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onCustomerCreated(objectMapper.writeValueAsString(event));

        verify(phoneStore).upsert(CUSTOMER_ID, PHONE);
        verify(notificationService).send(eq(PHONE), contains("Welcome"));
    }

    @Test
    void onCustomerCreated_shouldUnwrapSnsEnvelopeAndProcess() throws Exception {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                CUSTOMER_ID, "Anna", "Smith", "anna@smith.com", PHONE, ADDRESS, LocalDateTime.now(ZoneOffset.UTC));
        String snsEnvelope = objectMapper.writeValueAsString(
                Map.of("Type", "Notification", "Message", objectMapper.writeValueAsString(event)));

        consumer.onCustomerCreated(snsEnvelope);

        verify(phoneStore).upsert(CUSTOMER_ID, PHONE);
        verify(notificationService).send(eq(PHONE), contains("Welcome"));
    }

    @Test
    void onCustomerCreated_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onCustomerCreated("not-valid-json");

        verifyNoInteractions(phoneStore, notificationService);
    }

    @Test
    void onCustomerProfileUpdated_shouldUpsertPhone() throws Exception {
        CustomerProfileUpdatedEvent event = new CustomerProfileUpdatedEvent(CUSTOMER_ID, PHONE, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onCustomerProfileUpdated(objectMapper.writeValueAsString(event));

        verify(phoneStore).upsert(CUSTOMER_ID, PHONE);
    }

    @Test
    void onCustomerProfileUpdated_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onCustomerProfileUpdated("not-valid-json");

        verifyNoInteractions(phoneStore);
    }

    // --- Notification consumers ---

    @Test
    void onOrderPlaced_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(), CUSTOMER_ID, UUID.randomUUID(), BigDecimal.valueOf(25.00),
                List.of(new OrderItemDto(UUID.randomUUID(), "Pizza", 1, BigDecimal.valueOf(25.00))),
                ADDRESS, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderPlaced(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderPlaced_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(), CUSTOMER_ID, UUID.randomUUID(), BigDecimal.valueOf(25.00),
                List.of(new OrderItemDto(UUID.randomUUID(), "Pizza", 1, BigDecimal.valueOf(25.00))),
                ADDRESS, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderPlaced(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderPlaced_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderPlaced("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onOrderConfirmed_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderConfirmed(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderConfirmed_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderConfirmed("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onOrderConfirmed_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderConfirmed(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderInPreparation_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderInPreparationEvent event = new OrderInPreparationEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderInPreparation(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderInPreparation_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderInPreparation("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onOrderInPreparation_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderInPreparationEvent event = new OrderInPreparationEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderInPreparation(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderOnTheWay_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderOnTheWay(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderOnTheWay_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderOnTheWay("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onOrderOnTheWay_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderOnTheWay(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onOrderDelivered_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        OrderDeliveredEvent event = new OrderDeliveredEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderDelivered(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onOrderDelivered_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderDelivered("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onOrderDelivered_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        OrderDeliveredEvent event = new OrderDeliveredEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onOrderDelivered(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onRestaurantOrderCancelled_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onRestaurantOrderCancelled(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onRestaurantOrderCancelled_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onRestaurantOrderCancelled("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onRestaurantOrderCancelled_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        RestaurantOrderCancelledEvent event = new RestaurantOrderCancelledEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onRestaurantOrderCancelled(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }

    @Test
    void onDeliveryCancelled_whenPhoneKnown_shouldSendNotification() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(PHONE);
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onDeliveryCancelled(objectMapper.writeValueAsString(event));

        verify(notificationService).send(eq(PHONE), anyString());
    }

    @Test
    void onDeliveryCancelled_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onDeliveryCancelled("not-valid-json");

        verifyNoInteractions(notificationService);
    }

    @Test
    void onDeliveryCancelled_whenPhoneNull_shouldSkip() throws Exception {
        when(phoneStore.getOrWarn(CUSTOMER_ID)).thenReturn(null);
        DeliveryCancelledEvent event = new DeliveryCancelledEvent(UUID.randomUUID(), CUSTOMER_ID, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onDeliveryCancelled(objectMapper.writeValueAsString(event));

        verify(notificationService, never()).send(anyString(), anyString());
    }
}
