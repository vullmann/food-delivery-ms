package de.ullmann.fooddelivery.orderservice.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.messaging.SqsPayloadExtractor;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.service.CustomerOrderService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SqsOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsOrderEventConsumer.class);

    private final CustomerOrderService customerOrderService;
    private final ObjectMapper objectMapper;

    @SqsListener("order-svc-order-confirmed")
    public void onOrderConfirmed(String body) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderConfirmedEvent.class);
            log.info("SQS: OrderConfirmedEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.CONFIRMED);
        } catch (Exception e) {
            log.error("Failed to process OrderConfirmedEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-order-in-preparation")
    public void onOrderInPreparation(String body) {
        try {
            OrderInPreparationEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderInPreparationEvent.class);
            log.info("SQS: OrderInPreparationEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.PREPARING);
        } catch (Exception e) {
            log.error("Failed to process OrderInPreparationEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-order-ready-for-delivery")
    public void onOrderReadyForDelivery(String body) {
        try {
            OrderReadyForDeliveryEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderReadyForDeliveryEvent.class);
            log.info("SQS: OrderReadyForDeliveryEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.READY_FOR_DELIVERY);
        } catch (Exception e) {
            log.error("Failed to process OrderReadyForDeliveryEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-driver-assigned")
    public void onDriverAssigned(String body) {
        try {
            DriverAssignedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), DriverAssignedEvent.class);
            log.info("SQS: DriverAssignedEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.DRIVER_ASSIGNED);
        } catch (Exception e) {
            log.error("Failed to process DriverAssignedEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-order-on-the-way")
    public void onOrderOnTheWay(String body) {
        try {
            OrderOnTheWayEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderOnTheWayEvent.class);
            log.info("SQS: OrderOnTheWayEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.ON_THE_WAY);
        } catch (Exception e) {
            log.error("Failed to process OrderOnTheWayEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-order-delivered")
    public void onOrderDelivered(String body) {
        try {
            OrderDeliveredEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderDeliveredEvent.class);
            log.info("SQS: OrderDeliveredEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.DELIVERED);
        } catch (Exception e) {
            log.error("Failed to process OrderDeliveredEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-restaurant-order-cancelled")
    public void onRestaurantOrderCancelled(String body) {
        try {
            RestaurantOrderCancelledEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), RestaurantOrderCancelledEvent.class);
            log.info("SQS: RestaurantOrderCancelledEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.CANCELLED);
        } catch (Exception e) {
            log.error("Failed to process RestaurantOrderCancelledEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("order-svc-delivery-cancelled")
    public void onDeliveryCancelled(String body) {
        try {
            DeliveryCancelledEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), DeliveryCancelledEvent.class);
            log.info("SQS: DeliveryCancelledEvent orderId={}", event.orderId());
            customerOrderService.updateStatus(event.orderId(), CustomerOrderStatus.CANCELLED);
        } catch (Exception e) {
            log.error("Failed to process DeliveryCancelledEvent: {}", e.getMessage(), e);
        }
    }
}
