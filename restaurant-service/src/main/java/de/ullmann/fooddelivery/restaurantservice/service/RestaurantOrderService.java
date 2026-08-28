package de.ullmann.fooddelivery.restaurantservice.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.OrderConfirmedEvent;
import de.ullmann.fooddelivery.common.event.OrderInPreparationEvent;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.event.RestaurantOrderCancelledEvent;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.common.security.Role;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderItem;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderStatus;
import de.ullmann.fooddelivery.restaurantservice.exception.InsufficientRoleException;
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
    private static final String SUPER_ADMIN_AUTHORITY = "ROLE_" + Role.SUPER_ADMIN;
    private static final String RESTAURANT_ADMIN_AUTHORITY = "ROLE_" + Role.RESTAURANT_ADMIN;
    private static final String RESTAURANT_EMPLOYEE_AUTHORITY = "ROLE_" + Role.RESTAURANT_EMPLOYEE;

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
        assertCallerManagesRestaurantOrders();

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
                            restaurantOrder.getCustomerId(), LocalDateTime.now(ZoneOffset.UTC)));
            case PREPARING -> outboxEventService.createEvent(
                    AGGREGATE_TYPE_RESTAURANT_ORDER,
                    restaurantOrder.getCustomerOrderId(),
                    OrderInPreparationEvent.TOPIC,
                    new OrderInPreparationEvent(restaurantOrder.getCustomerOrderId(),
                            restaurantOrder.getCustomerId(), LocalDateTime.now(ZoneOffset.UTC)));
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
                                restaurant.getAddress(), restaurantOrder.getDeliveryAddress(), LocalDateTime.now(ZoneOffset.UTC)));
            }
            case CANCELLED -> outboxEventService.createEvent(
                    AGGREGATE_TYPE_RESTAURANT_ORDER,
                    restaurantOrder.getCustomerOrderId(),
                    RestaurantOrderCancelledEvent.TOPIC,
                    new RestaurantOrderCancelledEvent(restaurantOrder.getCustomerOrderId(),
                            restaurantOrder.getCustomerId(), LocalDateTime.now(ZoneOffset.UTC)));
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
        assertCallerManagesRestaurantOrders();
        return restaurantOrderRepository.findAllByRestaurantId(restaurantId);
    }

    // Restaurant orders may only be viewed/updated by SUPER_ADMIN, RESTAURANT_ADMIN or RESTAURANT_EMPLOYEE;
    // RESTAURANT_EMPLOYEE may not perform restaurant/menu-item CRUD (see RestaurantService). No authentication
    // in context (e.g. internal Kafka consumer calls to receiveOrder/markAsPickedUp, plain unit tests) is
    // treated as unrestricted.
    private void assertCallerManagesRestaurantOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        boolean allowed = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(SUPER_ADMIN_AUTHORITY)
                        || a.getAuthority().equals(RESTAURANT_ADMIN_AUTHORITY)
                        || a.getAuthority().equals(RESTAURANT_EMPLOYEE_AUTHORITY));
        if (!allowed) {
            throw new InsufficientRoleException("Only restaurant staff may access restaurant orders");
        }
    }
}