package de.ullmann.fooddelivery.restaurantservice.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.serializer.DeserializationException;

import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private RestaurantOrderService restaurantOrderService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void onOrderPlaced_whenExIsNull_shouldCallReceiveOrder() {
        OrderPlacedEvent event = mock(OrderPlacedEvent.class);

        orderEventConsumer.onOrderPlaced(event, null);

        verify(restaurantOrderService).receiveOrder(event);
    }

    @Test
    void onOrderPlaced_whenExIsNotNull_shouldNotCallReceiveOrder() {
        DeserializationException ex = new DeserializationException("deserialization failed", new byte[0], false, new RuntimeException());

        orderEventConsumer.onOrderPlaced(null, ex);

        verify(restaurantOrderService, never()).receiveOrder(any());
    }
}
