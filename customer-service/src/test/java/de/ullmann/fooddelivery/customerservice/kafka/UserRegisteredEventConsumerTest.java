package de.ullmann.fooddelivery.customerservice.kafka;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.customerservice.service.CustomerService;

@ExtendWith(MockitoExtension.class)
class UserRegisteredEventConsumerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private UserRegisteredEventConsumer consumer;

    @Test
    void onUserRegistered_shouldDelegateToCustomerService_whenRoleIsCustomer() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(), "CUSTOMER", "Jane", "Doe",
                "jane@example.com", "+49123456789", null, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onUserRegistered(event, null);

        verify(customerService).registerFromEvent(event);
    }

    @Test
    void onUserRegistered_shouldIgnoreOtherRoles() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(), "DELIVERY_DRIVER", "Max", "Müller",
                "max@example.com", "+49 30 11111111", null, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onUserRegistered(event, null);

        verifyNoInteractions(customerService);
    }

    @Test
    void onUserRegistered_shouldSkipProcessing_whenDeserializationExceptionPresent() {
        org.springframework.kafka.support.serializer.DeserializationException ex =
                new org.springframework.kafka.support.serializer.DeserializationException(
                        "error", new byte[0], false, new RuntimeException("bad json"));

        consumer.onUserRegistered(null, ex);

        verifyNoInteractions(customerService);
    }
}
