package de.ullmann.fooddelivery.deliverservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.deliverservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
@Profile("!aws")
public class OrderReadyConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderReadyConsumer.class);
    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = OrderReadyForDeliveryEvent.TOPIC,
            groupId = "delivery-service-group",
            containerFactory = "orderReadyFactory")
    public void onOrderReady(
            OrderReadyForDeliveryEvent event,
            @Header(value = "springDeserializedValueException", required = false)
            DeserializationException ex) {

        if (ex != null) {
            log.error("Deserialization error: {}", ex.getMessage());
            return;
        }

        log.info("Received OrderReadyForDeliveryEvent for orderId={}", event.orderId());
        deliveryService.receiveOrder(event);
    }
}
