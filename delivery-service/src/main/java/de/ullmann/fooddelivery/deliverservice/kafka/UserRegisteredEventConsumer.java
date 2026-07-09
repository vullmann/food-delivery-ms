package de.ullmann.fooddelivery.deliverservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.deliverservice.service.DriverService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
@Profile("!aws")
public class UserRegisteredEventConsumer {

    private static final String ROLE_DELIVERY_DRIVER = "DELIVERY_DRIVER";

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventConsumer.class);
    private final DriverService driverService;

    @KafkaListener(
            topics = UserRegisteredEvent.TOPIC,
            groupId = "delivery-service-group",
            containerFactory = "userRegisteredFactory")
    public void onUserRegistered(
            UserRegisteredEvent event,
            @Header(value = "springDeserializedValueException", required = false)
            DeserializationException ex) {

        if (ex != null) {
            log.error("Deserialization error: {}", ex.getMessage());
            return;
        }

        if (!ROLE_DELIVERY_DRIVER.equals(event.role())) {
            return;
        }

        log.info("Received UserRegisteredEvent for userId={}", event.userId());
        driverService.registerFromEvent(event);
    }
}
