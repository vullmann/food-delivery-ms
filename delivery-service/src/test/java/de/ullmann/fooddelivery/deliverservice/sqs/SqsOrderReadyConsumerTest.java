package de.ullmann.fooddelivery.deliverservice.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.deliverservice.service.DeliveryService;

@ExtendWith(MockitoExtension.class)
class SqsOrderReadyConsumerTest {

    @Mock
    private DeliveryService deliveryService;

    private ObjectMapper objectMapper;
    private SqsOrderReadyConsumer consumer;
    private OrderReadyForDeliveryEvent event;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new SqsOrderReadyConsumer(deliveryService, objectMapper);

        event = new OrderReadyForDeliveryEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("Restaurant St", "5", "Berlin", "10119", "Germany"),
                Address.of("Main St", "1", "Berlin", "10115", "Germany"),
                LocalDateTime.of(2026, 6, 1, 12, 0, 0)
        );
    }

    @Test
    void onOrderReady_shouldParseBodyAndDelegateToDeliveryService() throws Exception {
        String body = objectMapper.writeValueAsString(event);

        consumer.onOrderReady(body);

        OrderReadyForDeliveryEvent captured = captureReceivedEvent();
        assertThat(captured.orderId()).isEqualTo(event.orderId());
        assertThat(captured.customerId()).isEqualTo(event.customerId());
        assertThat(captured.restaurantId()).isEqualTo(event.restaurantId());
        assertThat(captured.readyAt()).isEqualTo(event.readyAt());
        assertThat(captured.pickupAddress().getStreet()).isEqualTo(event.pickupAddress().getStreet());
        assertThat(captured.deliveryAddress().getStreet()).isEqualTo(event.deliveryAddress().getStreet());
    }

    @Test
    void onOrderReady_shouldUnwrapSnsEnvelopeAndDelegateToDeliveryService() throws Exception {
        String eventJson = objectMapper.writeValueAsString(event);
        String snsEnvelope = objectMapper.writeValueAsString(
                Map.of("Type", "Notification", "Message", eventJson)
        );

        consumer.onOrderReady(snsEnvelope);

        OrderReadyForDeliveryEvent captured = captureReceivedEvent();
        assertThat(captured.orderId()).isEqualTo(event.orderId());
        assertThat(captured.customerId()).isEqualTo(event.customerId());
        assertThat(captured.restaurantId()).isEqualTo(event.restaurantId());
        assertThat(captured.readyAt()).isEqualTo(event.readyAt());
    }

    @Test
    void onOrderReady_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderReady("not-valid-json");

        verifyNoInteractions(deliveryService);
    }

    private OrderReadyForDeliveryEvent captureReceivedEvent() {
        ArgumentCaptor<OrderReadyForDeliveryEvent> captor = ArgumentCaptor.forClass(OrderReadyForDeliveryEvent.class);
        verify(deliveryService).receiveOrder(captor.capture());
        return captor.getValue();
    }
}
