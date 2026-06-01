package de.ullmann.fooddelivery.orderservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.service.CustomerOrderService;

@Component
@Profile("!aws")
public class CustomerOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CustomerOrderEventConsumer.class);

    private final CustomerOrderService customerOrderService;

    public CustomerOrderEventConsumer(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @KafkaListener(topics = OrderConfirmedEvent.TOPIC, groupId = "order-service-group", containerFactory = "orderConfirmedFactory")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.CONFIRMED);
    }

    @KafkaListener(topics = OrderInPreparationEvent.TOPIC, groupId = "order-service-group", containerFactory = "orderInPreparationFactory")
    public void onOrderInPreparation(OrderInPreparationEvent event) {
        log.info("Received OrderInPreparationEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.PREPARING);
    }

    @KafkaListener(topics = OrderReadyForDeliveryEvent.TOPIC, groupId = "order-service-group", containerFactory = "orderReadyForDeliveryFactory")
    public void onOrderReadyForDelivery(OrderReadyForDeliveryEvent event) {
        log.info("Received OrderReadyForDeliveryEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.READY_FOR_DELIVERY);
    }

    @KafkaListener(topics = DriverAssignedEvent.TOPIC, groupId = "order-service-group", containerFactory = "driverAssignedFactory")
    public void onDriverAssigned(DriverAssignedEvent event) {
        log.info("Received DriverAssignedEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.DRIVER_ASSIGNED);
    }

    @KafkaListener(topics = OrderOnTheWayEvent.TOPIC, groupId = "order-service-group", containerFactory = "orderOnTheWayFactory")
    public void onOrderOnTheWay(OrderOnTheWayEvent event) {
        log.info("Received OrderOnTheWayEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.ON_THE_WAY);
    }

    @KafkaListener(topics = OrderDeliveredEvent.TOPIC, groupId = "order-service-group", containerFactory = "orderDeliveredFactory")
    public void onOrderDelivered(OrderDeliveredEvent event) {
        log.info("Received OrderDeliveredEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.DELIVERED);
    }

    @KafkaListener(topics = RestaurantOrderCancelledEvent.TOPIC, groupId = "order-service-group", containerFactory = "restaurantOrderCancelledFactory")
    public void onRestaurantOrderCancelled(RestaurantOrderCancelledEvent event) {
        log.info("Received RestaurantOrderCancelledEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.CANCELLED);
    }

    @KafkaListener(topics = DeliveryCancelledEvent.TOPIC, groupId = "order-service-group", containerFactory = "deliveryCancelledFactory")
    public void onDeliveryCancelled(DeliveryCancelledEvent event) {
        log.info("Received DeliveryCancelledEvent for orderId={}", event.orderId());
        customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.CANCELLED);
    }
}