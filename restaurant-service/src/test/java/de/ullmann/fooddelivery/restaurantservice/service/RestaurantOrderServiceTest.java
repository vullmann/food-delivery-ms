package de.ullmann.fooddelivery.restaurantservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderItemDto;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderStatus;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantOrderAccessDeniedException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantOrderNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.repository.RestaurantOrderRepository;
import de.ullmann.fooddelivery.restaurantservice.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class RestaurantOrderServiceTest {

    @Mock
    private RestaurantOrderRepository restaurantOrderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private RestaurantOrderService restaurantOrderService;

    private UUID customerOrderId;
    private UUID restaurantId;
    private UUID customerId;
    private UUID restaurantOrderId;
    private Address deliveryAddress;
    private OrderPlacedEvent orderPlacedEvent;

    @BeforeEach
    void setUp() {
        customerOrderId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        restaurantOrderId = UUID.randomUUID();
        deliveryAddress = Address.of("Main St", "1", "Berlin", "10115", "Germany");

        orderPlacedEvent = new OrderPlacedEvent(
                customerOrderId,
                customerId,
                restaurantId,
                new BigDecimal("19.90"),
                List.of(new OrderItemDto(UUID.randomUUID(), "Margherita", 1, new BigDecimal("19.90"))),
                deliveryAddress,
                LocalDateTime.now());
    }

    // ── receiveOrder ──────────────────────────────────────────────────────────

    @Test
    void receiveOrder_shouldSaveOrderWithReceivedStatus() {
        ArgumentCaptor<RestaurantOrder> captor = ArgumentCaptor.forClass(RestaurantOrder.class);

        restaurantOrderService.receiveOrder(orderPlacedEvent);

        verify(restaurantOrderRepository).save(captor.capture());
        RestaurantOrder restaurantOrder = captor.getValue();
        assertThat(restaurantOrder.getCustomerOrderId()).isEqualTo(customerOrderId);
        assertThat(restaurantOrder.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(restaurantOrder.getCustomerId()).isEqualTo(customerId);
        assertThat(restaurantOrder.getStatus()).isEqualTo(RestaurantOrderStatus.RECEIVED);
    }

    @Test
    void receiveOrder_shouldSaveOrderWithItems() {
        ArgumentCaptor<RestaurantOrder> captor = ArgumentCaptor.forClass(RestaurantOrder.class);

        restaurantOrderService.receiveOrder(orderPlacedEvent);

        verify(restaurantOrderRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).hasSize(1);
        assertThat(captor.getValue().getItems().get(0).getName()).isEqualTo("Margherita");
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_shouldThrow_whenOrderNotFound() {
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, RestaurantOrderStatus.CONFIRMED))
                .isInstanceOf(RestaurantOrderNotFoundException.class);

        verify(outboxEventService, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void updateStatus_shouldThrow_whenOrderDoesNotBelongToRestaurant() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        UUID differentRestaurantId = UUID.randomUUID();
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                restaurantOrderService.updateStatus(differentRestaurantId, restaurantOrderId, RestaurantOrderStatus.CONFIRMED))
                .isInstanceOf(RestaurantOrderAccessDeniedException.class);

        verify(outboxEventService, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void updateStatus_toConfirmed_shouldPublishOrderConfirmedEvent() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.of(order));

        restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, RestaurantOrderStatus.CONFIRMED);

        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.CONFIRMED);
        verify(outboxEventService).createEvent(
                eq("RestaurantOrder"), eq(customerOrderId),
                eq(OrderConfirmedEvent.TOPIC), any(OrderConfirmedEvent.class));
    }

    @Test
    void updateStatus_toPreparing_shouldPublishOrderInPreparationEvent() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        order.transitionTo(RestaurantOrderStatus.CONFIRMED);
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.of(order));

        restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, RestaurantOrderStatus.PREPARING);

        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.PREPARING);
        verify(outboxEventService).createEvent(
                eq("RestaurantOrder"), eq(customerOrderId),
                eq(OrderInPreparationEvent.TOPIC), any(OrderInPreparationEvent.class));
    }

    @Test
    void updateStatus_toReadyForDelivery_shouldPublishOrderReadyForDeliveryEvent() {
        Address restaurantAddress = Address.of("Restaurant St", "5", "Berlin", "10115", "Germany");
        Restaurant restaurant = Restaurant.create("Pizza Roma", "desc", restaurantAddress,
                "+49123", "pizza@roma.de", CuisineType.PIZZA, true);

        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        order.transitionTo(RestaurantOrderStatus.CONFIRMED);
        order.transitionTo(RestaurantOrderStatus.PREPARING);
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.of(order));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, RestaurantOrderStatus.READY_FOR_DELIVERY);

        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.READY_FOR_DELIVERY);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).createEvent(
                eq("RestaurantOrder"), eq(customerOrderId),
                eq(OrderReadyForDeliveryEvent.TOPIC), eventCaptor.capture());
        OrderReadyForDeliveryEvent event = (OrderReadyForDeliveryEvent) eventCaptor.getValue();
        assertThat(event.orderId()).isEqualTo(customerOrderId);
        assertThat(event.restaurantId()).isEqualTo(restaurantId);
        assertThat(event.pickupAddress()).usingRecursiveComparison().isEqualTo(restaurantAddress);
        assertThat(event.deliveryAddress()).usingRecursiveComparison().isEqualTo(deliveryAddress);
    }

    @Test
    void updateStatus_toReadyForDelivery_shouldThrow_whenRestaurantNotFound() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        order.transitionTo(RestaurantOrderStatus.CONFIRMED);
        order.transitionTo(RestaurantOrderStatus.PREPARING);
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.of(order));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, RestaurantOrderStatus.READY_FOR_DELIVERY))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // ── markAsPickedUp ────────────────────────────────────────────────────────

    @Test
    void markAsPickedUp_shouldTransitionStatusToPickedUp() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        order.transitionTo(RestaurantOrderStatus.CONFIRMED);
        order.transitionTo(RestaurantOrderStatus.PREPARING);
        order.transitionTo(RestaurantOrderStatus.READY_FOR_DELIVERY);
        when(restaurantOrderRepository.findByCustomerOrderId(customerOrderId)).thenReturn(Optional.of(order));

        restaurantOrderService.markAsPickedUp(customerOrderId);

        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.PICKED_UP);
        verify(outboxEventService, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void markAsPickedUp_shouldThrow_whenOrderNotFound() {
        when(restaurantOrderRepository.findByCustomerOrderId(customerOrderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantOrderService.markAsPickedUp(customerOrderId))
                .isInstanceOf(RestaurantOrderNotFoundException.class);
    }

    @Test
    void updateStatus_toCancelled_shouldPublishRestaurantOrderCancelledEvent() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        when(restaurantOrderRepository.findById(restaurantOrderId)).thenReturn(Optional.of(order));

        restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, RestaurantOrderStatus.CANCELLED);

        assertThat(order.getStatus()).isEqualTo(RestaurantOrderStatus.CANCELLED);
        verify(outboxEventService).createEvent(
                eq("RestaurantOrder"), eq(customerOrderId),
                eq(RestaurantOrderCancelledEvent.TOPIC), any(RestaurantOrderCancelledEvent.class));
    }

    // ── findByRestaurant ──────────────────────────────────────────────────────

    @Test
    void findByRestaurant_shouldReturnOrdersForRestaurant() {
        RestaurantOrder order = RestaurantOrder.create(customerOrderId, restaurantId, customerId, deliveryAddress);
        when(restaurantOrderRepository.findAllByRestaurantId(restaurantId)).thenReturn(List.of(order));

        List<RestaurantOrder> result = restaurantOrderService.findByRestaurant(restaurantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRestaurantId()).isEqualTo(restaurantId);
    }

    @Test
    void findByRestaurant_shouldReturnEmptyList_whenNoOrders() {
        when(restaurantOrderRepository.findAllByRestaurantId(restaurantId)).thenReturn(List.of());

        List<RestaurantOrder> result = restaurantOrderService.findByRestaurant(restaurantId);

        assertThat(result).isEmpty();
    }
}
