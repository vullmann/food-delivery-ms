package de.ullmann.fooddelivery.restaurantservice.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.OrderItemDto;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;

@ExtendWith(MockitoExtension.class)
class SqsOrderEventConsumerTest {

    @Mock
    private RestaurantOrderService restaurantOrderService;

    private ObjectMapper objectMapper;
    private SqsOrderEventConsumer consumer;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final Address ADDRESS = Address.of("Main St", "1", "Berlin", "10115", "Germany");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new SqsOrderEventConsumer(restaurantOrderService, objectMapper);
    }

    @Test
    void onOrderPlaced_shouldDelegateToRestaurantOrderService() throws Exception {
        OrderPlacedEvent event = new OrderPlacedEvent(
                ORDER_ID, CUSTOMER_ID, RESTAURANT_ID, BigDecimal.valueOf(25.00),
                List.of(new OrderItemDto(UUID.randomUUID(), "Pizza", 1, BigDecimal.valueOf(25.00))),
                ADDRESS, LocalDateTime.now());

        consumer.onOrderPlaced(objectMapper.writeValueAsString(event));

        ArgumentCaptor<OrderPlacedEvent> captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(restaurantOrderService).receiveOrder(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().restaurantId()).isEqualTo(RESTAURANT_ID);
    }

    @Test
    void onOrderPlaced_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderPlaced("not-valid-json");

        verifyNoInteractions(restaurantOrderService);
    }

    @Test
    void onOrderOnTheWay_shouldCallMarkAsPickedUp() throws Exception {
        OrderOnTheWayEvent event = new OrderOnTheWayEvent(ORDER_ID, CUSTOMER_ID, LocalDateTime.now());

        consumer.onOrderOnTheWay(objectMapper.writeValueAsString(event));

        verify(restaurantOrderService).markAsPickedUp(ORDER_ID);
    }

    @Test
    void onOrderOnTheWay_shouldNotThrow_whenBodyIsInvalidJson() {
        consumer.onOrderOnTheWay("not-valid-json");

        verifyNoInteractions(restaurantOrderService);
    }
}
