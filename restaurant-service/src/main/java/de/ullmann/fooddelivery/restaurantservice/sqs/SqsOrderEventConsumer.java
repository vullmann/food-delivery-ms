package de.ullmann.fooddelivery.restaurantservice.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.messaging.SqsPayloadExtractor;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SqsOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsOrderEventConsumer.class);

    private final RestaurantOrderService restaurantOrderService;
    private final ObjectMapper objectMapper;

    @SqsListener("restaurant-svc-order-placed")
    public void onOrderPlaced(String body) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderPlacedEvent.class);
            log.info("SQS: OrderPlacedEvent orderId={}", event.orderId());
            restaurantOrderService.receiveOrder(event);
        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent: {}", e.getMessage(), e);
        }
    }

    @SqsListener("restaurant-svc-order-on-the-way")
    public void onOrderOnTheWay(String body) {
        try {
            OrderOnTheWayEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderOnTheWayEvent.class);
            log.info("SQS: OrderOnTheWayEvent orderId={}", event.orderId());
            restaurantOrderService.markAsPickedUp(event.orderId());
        } catch (Exception e) {
            log.error("Failed to process OrderOnTheWayEvent: {}", e.getMessage(), e);
        }
    }
}
