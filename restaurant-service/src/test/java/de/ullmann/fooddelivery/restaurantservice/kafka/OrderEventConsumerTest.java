package de.ullmann.fooddelivery.restaurantservice.kafka;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private RestaurantOrderService restaurantOrderService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    /*

    @Test
    void shouldCallReceiveOrderWhenOrderPlacedEventReceived() {
        // Arrange

        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));

        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("45.50"),
                List.of(item),
                Address.of("Musterstraße 1", "23", "Halle", "06108", "DE"),
                LocalDateTime.now()
        );

        // Act
        orderEventConsumer.onOrderPlaced(event);

        // Assert
        verify(restaurantOrderService).receiveOrder(event);
    }

    @Test
    void shouldStillCallServiceWhenEventHasNullOrderId() {
        OrderPlacedEvent event = mock(OrderPlacedEvent.class);
        when(event.orderId()).thenReturn(null);

        orderEventConsumer.onOrderPlaced(event);

        verify(restaurantOrderService).receiveOrder(event);
    }

     */
}