package de.ullmann.fooddelivery.restaurantservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderItem;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderStatus;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantOrderAccessDeniedException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantOrderNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.repository.RestaurantOrderRepository;
import de.ullmann.fooddelivery.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantOrderService {

    private static final String AGGREGATE_TYPE_RESTAURANT_ORDER = "RestaurantOrder";

    private final RestaurantOrderRepository restaurantOrderRepository;
    private final RestaurantRepository restaurantRepository;
    private final OutboxEventService outboxEventService;


    public void receiveOrder(OrderPlacedEvent event) {
        RestaurantOrder restaurantOrder = RestaurantOrder.create(
                event.orderId(), event.restaurantId(), event.customerId(), event.deliveryAddress());

        event.items().forEach(dto ->
                restaurantOrder.addItem(RestaurantOrderItem.create(
                        dto.menuItemId(), dto.name(), dto.quantity(), dto.price())));

        restaurantOrderRepository.save(restaurantOrder);
    }

    public void updateStatus(
            UUID restaurantId,
            UUID restaurantOrderId,
            RestaurantOrderStatus newStatus) {

        RestaurantOrder restaurantOrder = restaurantOrderRepository.findById(restaurantOrderId)
                .orElseThrow(() -> new RestaurantOrderNotFoundException(restaurantOrderId));

        if (!restaurantOrder.getRestaurantId().equals(restaurantId)) {
            throw new RestaurantOrderAccessDeniedException(restaurantOrderId, restaurantId);
        }
        restaurantOrder.transitionTo(newStatus);

        switch (newStatus) {
            case CONFIRMED -> outboxEventService.createEvent(
                    AGGREGATE_TYPE_RESTAURANT_ORDER,
                    restaurantOrder.getCustomerOrderId(),
                    OrderConfirmedEvent.TOPIC,
                    new OrderConfirmedEvent(restaurantOrder.getCustomerOrderId(),
                            restaurantOrder.getCustomerId(), LocalDateTime.now()));
            case PREPARING -> outboxEventService.createEvent(
                    AGGREGATE_TYPE_RESTAURANT_ORDER,
                    restaurantOrder.getCustomerOrderId(),
                    OrderInPreparationEvent.TOPIC,
                    new OrderInPreparationEvent(restaurantOrder.getCustomerOrderId(),
                            restaurantOrder.getCustomerId(), LocalDateTime.now()));
            case READY_FOR_DELIVERY -> {
                Restaurant restaurant = restaurantRepository.findById(restaurantOrder.getRestaurantId())
                        .orElseThrow(() -> new RestaurantNotFoundException(restaurantOrder.getRestaurantId()));
                outboxEventService.createEvent(
                        AGGREGATE_TYPE_RESTAURANT_ORDER,
                        restaurantOrder.getCustomerOrderId(),
                        OrderReadyForDeliveryEvent.TOPIC,
                        new OrderReadyForDeliveryEvent(
                                restaurantOrder.getCustomerOrderId(), restaurantOrder.getCustomerId(),
                                restaurantOrder.getRestaurantId(),
                                restaurant.getAddress(), restaurantOrder.getDeliveryAddress(), LocalDateTime.now()));
            }
            case CANCELLED -> outboxEventService.createEvent(
                    AGGREGATE_TYPE_RESTAURANT_ORDER,
                    restaurantOrder.getCustomerOrderId(),
                    RestaurantOrderCancelledEvent.TOPIC,
                    new RestaurantOrderCancelledEvent(restaurantOrder.getCustomerOrderId(),
                            restaurantOrder.getCustomerId(), LocalDateTime.now()));
            default -> {
            }
        }
    }

    public void markAsPickedUp(UUID customerOrderId) {
        RestaurantOrder order = restaurantOrderRepository.findByCustomerOrderId(customerOrderId)
                .orElseThrow(() -> new RestaurantOrderNotFoundException(customerOrderId));
        order.transitionTo(RestaurantOrderStatus.PICKED_UP);
    }

    @Transactional(readOnly = true)
    public List<RestaurantOrder> findByRestaurant(UUID restaurantId) {
        return restaurantOrderRepository.findAllByRestaurantId(restaurantId);
    }
}