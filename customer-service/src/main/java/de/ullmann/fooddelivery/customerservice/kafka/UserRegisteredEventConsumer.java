package de.ullmann.fooddelivery.customerservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.security.Role;
import de.ullmann.fooddelivery.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
@Profile("!aws")
public class UserRegisteredEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventConsumer.class);
    private final CustomerService customerService;

    @KafkaListener(
            topics = UserRegisteredEvent.TOPIC,
            groupId = "customer-service-group",
            containerFactory = "userRegisteredFactory")
    public void onUserRegistered(
            UserRegisteredEvent event,
            @Header(value = "springDeserializedValueException", required = false)
            DeserializationException ex) {

        if (ex != null) {
            log.error("Deserialization error: {}", ex.getMessage());
            return;
        }

        if (!Role.CUSTOMER.name().equals(event.role())) {
            return;
        }

        log.info("Received UserRegisteredEvent for userId={}", event.userId());
        customerService.registerFromEvent(event);
    }
}
