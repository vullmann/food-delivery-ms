package de.ullmann.fooddelivery.customerservice.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.messaging.SqsPayloadExtractor;
import de.ullmann.fooddelivery.common.security.Role;
import de.ullmann.fooddelivery.customerservice.service.CustomerService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SqsUserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsUserRegisteredConsumer.class);

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @SqsListener("customer-svc-user-registered")
    public void onUserRegistered(String body) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(
                    SqsPayloadExtractor.extract(body, objectMapper), UserRegisteredEvent.class);

            if (!Role.CUSTOMER.name().equals(event.role())) {
                return;
            }

            log.info("SQS: UserRegisteredEvent userId={}", event.userId());
            customerService.registerFromEvent(event);
        } catch (Exception e) {
            log.error("Failed to process UserRegisteredEvent: {}", e.getMessage(), e);
        }
    }
}
