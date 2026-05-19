package de.ullmann.fooddelivery.deliverservice.kafka;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.deliverservice.service.DeliveryService;

@ExtendWith(MockitoExtension.class)
class OrderReadyConsumerTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private OrderReadyConsumer orderReadyConsumer;

    @Test
    void onOrderReady_shouldDelegateToDeliveryService() {
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Address.of("Restaurant St", "5", "Berlin", "10119", "Germany"),
                Address.of("Main St", "1", "Berlin", "10115", "Germany"),
                LocalDateTime.now());

        orderReadyConsumer.onOrderReady(event, null);

        verify(deliveryService).receiveOrder(event);
    }

    @Test
    void onOrderReady_shouldSkipProcessing_whenDeserializationExceptionPresent() {
        org.springframework.kafka.support.serializer.DeserializationException ex =
                new org.springframework.kafka.support.serializer.DeserializationException(
                        "error", new byte[0], false, new RuntimeException("bad json"));

        orderReadyConsumer.onOrderReady(null, ex);

        org.mockito.Mockito.verifyNoInteractions(deliveryService);
    }
}
