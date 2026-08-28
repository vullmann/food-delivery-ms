package de.ullmann.fooddelivery.deliverservice.kafka;

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
import de.ullmann.fooddelivery.deliverservice.service.DriverService;

@ExtendWith(MockitoExtension.class)
class UserRegisteredEventConsumerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private UserRegisteredEventConsumer consumer;

    @Test
    void onUserRegistered_shouldDelegateToDriverService_whenRoleIsDeliveryDriver() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(), "DELIVERY_DRIVER", "Max", "Müller",
                "max@example.com", "+49 30 11111111", null, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onUserRegistered(event, null);

        verify(driverService).registerFromEvent(event);
    }

    @Test
    void onUserRegistered_shouldIgnoreOtherRoles() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(), "CUSTOMER", "Jane", "Doe",
                "jane@example.com", "+49123456789", null, LocalDateTime.now(ZoneOffset.UTC));

        consumer.onUserRegistered(event, null);

        verifyNoInteractions(driverService);
    }

    @Test
    void onUserRegistered_shouldSkipProcessing_whenDeserializationExceptionPresent() {
        org.springframework.kafka.support.serializer.DeserializationException ex =
                new org.springframework.kafka.support.serializer.DeserializationException(
                        "error", new byte[0], false, new RuntimeException("bad json"));

        consumer.onUserRegistered(null, ex);

        verifyNoInteractions(driverService);
    }
}
