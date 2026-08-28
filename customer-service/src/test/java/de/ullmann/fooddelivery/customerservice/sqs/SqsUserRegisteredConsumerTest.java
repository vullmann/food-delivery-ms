package de.ullmann.fooddelivery.customerservice.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.customerservice.service.CustomerService;

@ExtendWith(MockitoExtension.class)
class SqsUserRegisteredConsumerTest {

    @Mock
    private CustomerService customerService;

    private ObjectMapper objectMapper;
    private SqsUserRegisteredConsumer consumer;
    private UserRegisteredEvent event;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new SqsUserRegisteredConsumer(customerService, objectMapper);

        event = new UserRegisteredEvent(
                UUID.randomUUID(), "CUSTOMER", "Jane", "Doe",
                "jane@example.com", "+49123456789", null,
                LocalDateTime.of(2026, 6, 1, 12, 0, 0));
    }

    @Test
    void onUserRegistered_shouldParseBodyAndDelegateToCustomerService() throws Exception {
        String body = objectMapper.writeValueAsString(event);

        consumer.onUserRegistered(body);

        ArgumentCaptor<UserRegisteredEvent> captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(customerService).registerFromEvent(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(event.userId());
    }

    @Test
    void onUserRegistered_shouldIgnoreOtherRoles() throws Exception {
        UserRegisteredEvent driverEvent = new UserRegisteredEvent(
                UUID.randomUUID(), "DELIVERY_DRIVER", "Max", "Müller",
                "max@example.com", "+49 30 11111111", null, LocalDateTime.now(ZoneOffset.UTC));
        String body = objectMapper.writeValueAsString(driverEvent);

        consumer.onUserRegistered(body);

        verifyNoInteractions(customerService);
    }

    @Test
    void onUserRegistered_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onUserRegistered("not-valid-json");

        verifyNoInteractions(customerService);
    }
}
