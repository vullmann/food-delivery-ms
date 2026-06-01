package de.ullmann.fooddelivery.notificationservice.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.messaging.SqsPayloadExtractor;
import de.ullmann.fooddelivery.notificationservice.projection.CustomerPhoneStore;
import de.ullmann.fooddelivery.notificationservice.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SqsNotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsNotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final CustomerPhoneStore phoneStore;
    private final ObjectMapper objectMapper;

    // --- Projection maintenance ---

    @SqsListener("notification-svc-customer-created")
    public void onCustomerCreated(String body) {
        try {
            CustomerCreatedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), CustomerCreatedEvent.class);
            phoneStore.upsert(event.customerId(), event.phone());
            notificationService.send(event.phone(),
                    "Welcome to FoodDelivery, " + event.firstName() + "! Your account is ready.");
        } catch (Exception e) {
            log.error("Failed to process CustomerCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-customer-profile-updated")
    public void onCustomerProfileUpdated(String body) {
        try {
            CustomerProfileUpdatedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), CustomerProfileUpdatedEvent.class);
            phoneStore.upsert(event.customerId(), event.phone());
        } catch (Exception e) {
            log.error("Failed to process CustomerProfileUpdatedEvent: {}", e.getMessage(), e);
        }
    }

    // --- Notification consumers ---

    @SqsListener("notification-svc-order-placed")
    public void onOrderPlaced(String body) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderPlacedEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone,
                    "Your order has been placed! Total: " + event.totalAmount() +
                            " EUR. We will notify you once the restaurant confirms.");
        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-order-confirmed")
    public void onOrderConfirmed(String body) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderConfirmedEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone, "Your order has been confirmed by the restaurant!");
        } catch (Exception e) {
            log.error("Failed to process OrderConfirmedEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-order-in-preparation")
    public void onOrderInPreparation(String body) {
        try {
            OrderInPreparationEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderInPreparationEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone, "Your order is being prepared!");
        } catch (Exception e) {
            log.error("Failed to process OrderInPreparationEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-order-on-the-way")
    public void onOrderOnTheWay(String body) {
        try {
            OrderOnTheWayEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderOnTheWayEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone, "Your order is on the way!");
        } catch (Exception e) {
            log.error("Failed to process OrderOnTheWayEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-order-delivered")
    public void onOrderDelivered(String body) {
        try {
            OrderDeliveredEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderDeliveredEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone, "Your order has been delivered. Enjoy your meal!");
        } catch (Exception e) {
            log.error("Failed to process OrderDeliveredEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-restaurant-order-cancelled")
    public void onRestaurantOrderCancelled(String body) {
        try {
            RestaurantOrderCancelledEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), RestaurantOrderCancelledEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone,
                    "Unfortunately, the restaurant has cancelled your order. We are sorry for the inconvenience.");
        } catch (Exception e) {
            log.error("Failed to process RestaurantOrderCancelledEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("notification-svc-delivery-cancelled")
    public void onDeliveryCancelled(String body) {
        try {
            DeliveryCancelledEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), DeliveryCancelledEvent.class);
            String phone = phoneStore.getOrWarn(event.customerId());
            if (phone == null) return;
            notificationService.send(phone,
                    "Unfortunately, your delivery has been cancelled. Our team is looking into it.");
        } catch (Exception e) {
            log.error("Failed to process DeliveryCancelledEvent: {}", e.getMessage(), e);
        }
    }
}
