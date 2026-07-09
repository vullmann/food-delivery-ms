package de.ullmann.fooddelivery.deliverservice.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.messaging.SqsPayloadExtractor;
import de.ullmann.fooddelivery.deliverservice.service.DriverService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SqsUserRegisteredConsumer {

    private static final String ROLE_DELIVERY_DRIVER = "DELIVERY_DRIVER";

    private static final Logger log = LoggerFactory.getLogger(SqsUserRegisteredConsumer.class);

    private final DriverService driverService;
    private final ObjectMapper objectMapper;

    @SqsListener("delivery-svc-user-registered")
    public void onUserRegistered(String body) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(
                    SqsPayloadExtractor.extract(body, objectMapper), UserRegisteredEvent.class);

            if (!ROLE_DELIVERY_DRIVER.equals(event.role())) {
                return;
            }

            log.info("SQS: UserRegisteredEvent userId={}", event.userId());
            driverService.registerFromEvent(event);
        } catch (Exception e) {
            log.error("Failed to process UserRegisteredEvent: {}", e.getMessage(), e);
        }
    }
}
