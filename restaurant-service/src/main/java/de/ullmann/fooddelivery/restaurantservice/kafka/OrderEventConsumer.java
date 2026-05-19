package de.ullmann.fooddelivery.restaurantservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final RestaurantOrderService restaurantOrderService;


    @KafkaListener(
            topics = OrderPlacedEvent.TOPIC,
            groupId = "restaurant-service-group",
            containerFactory = "orderPlacedFactory")
    public void onOrderPlaced(
            OrderPlacedEvent event,
            @Header(value = "springDeserializedValueException", required = false)
            DeserializationException ex) {

        if (ex != null) {
            log.error("Deserialisierungsfehler: {}", ex.getMessage());
            // ex.getData() enthält sogar die rohen Bytes, die den Fehler verursacht haben!
            return;
        }

        log.info("Received OrderPlacedEvent for orderId={}", event.orderId());
        restaurantOrderService.receiveOrder(event);
    }
}