package de.ullmann.fooddelivery.deliverservice.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.messaging.SqsPayloadExtractor;
import de.ullmann.fooddelivery.deliverservice.service.DeliveryService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SqsOrderReadyConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsOrderReadyConsumer.class);

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @SqsListener("delivery-svc-order-ready-for-delivery")
    public void onOrderReady(String body) {
        try {
            OrderReadyForDeliveryEvent event = objectMapper.readValue(SqsPayloadExtractor.extract(body, objectMapper), OrderReadyForDeliveryEvent.class);
            log.info("SQS: OrderReadyForDeliveryEvent orderId={}", event.orderId());
            deliveryService.receiveOrder(event);
        } catch (Exception e) {
            log.error("Failed to process OrderReadyForDeliveryEvent: {}", e.getMessage(), e);
        }
    }
}
