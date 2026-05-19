package de.ullmann.fooddelivery.notificationservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.notificationservice.projection.CustomerPhoneStore;
import de.ullmann.fooddelivery.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final CustomerPhoneStore phoneStore;

    // --- Projection maintenance (separate group so this always replays from earliest on fresh deploy) ---

    @KafkaListener(topics = CustomerCreatedEvent.TOPIC, groupId = "notification-service-projection-group", containerFactory = "customerCreatedFactory")
    public void onCustomerCreated(CustomerCreatedEvent event) {
        phoneStore.upsert(event.customerId(), event.phone());
        notificationService.send(event.phone(),
                "Welcome to FoodDelivery, " + event.firstName() + "! Your account is ready.");
    }

    @KafkaListener(topics = CustomerProfileUpdatedEvent.TOPIC, groupId = "notification-service-projection-group", containerFactory = "customerProfileUpdatedFactory")
    public void onCustomerProfileUpdated(CustomerProfileUpdatedEvent event) {
        phoneStore.upsert(event.customerId(), event.phone());
    }

    // --- Notification consumers ---

    @KafkaListener(topics = OrderPlacedEvent.TOPIC, groupId = "notification-service-group", containerFactory = "orderPlacedFactory")
    public void onOrderPlaced(OrderPlacedEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone,
                "Your order has been placed! Total: " + event.totalAmount() +
                        " EUR. We will notify you once the restaurant confirms.");
    }

    @KafkaListener(topics = OrderConfirmedEvent.TOPIC, groupId = "notification-service-group", containerFactory = "orderConfirmedFactory")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone, "Your order has been confirmed by the restaurant!");
    }

    @KafkaListener(topics = OrderInPreparationEvent.TOPIC, groupId = "notification-service-group", containerFactory = "orderInPreparationFactory")
    public void onOrderInPreparation(OrderInPreparationEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone, "Your order is being prepared!");
    }

    @KafkaListener(topics = OrderReadyForDeliveryEvent.TOPIC, groupId = "notification-service-group", containerFactory = "orderInPreparationFactory")
    public void onOrderInPreparation(OrderReadyForDeliveryEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone, "Your order is ready for delivery!");
    }

    @KafkaListener(topics = OrderOnTheWayEvent.TOPIC, groupId = "notification-service-group", containerFactory = "orderOnTheWayFactory")
    public void onOrderOnTheWay(OrderOnTheWayEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone, "Your order is on the way!");
    }

    @KafkaListener(topics = OrderDeliveredEvent.TOPIC, groupId = "notification-service-group", containerFactory = "orderDeliveredFactory")
    public void onOrderDelivered(OrderDeliveredEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone, "Your order has been delivered. Enjoy your meal!");
    }

    @KafkaListener(topics = RestaurantOrderCancelledEvent.TOPIC, groupId = "notification-service-group", containerFactory = "restaurantOrderCancelledFactory")
    public void onRestaurantOrderCancelled(RestaurantOrderCancelledEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone,
                "Unfortunately, the restaurant has cancelled your order. We are sorry for the inconvenience.");
    }

    @KafkaListener(topics = DeliveryCancelledEvent.TOPIC, groupId = "notification-service-group", containerFactory = "deliveryCancelledFactory")
    public void onDeliveryCancelled(DeliveryCancelledEvent event) {
        String phone = phoneStore.getOrWarn(event.customerId());
        if (phone == null) return;
        notificationService.send(phone,
                "Unfortunately, your delivery has been cancelled. Our team is looking into it.");
    }
}
