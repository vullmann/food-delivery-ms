package de.ullmann.fooddelivery.deliverservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.common.security.Role;
import de.ullmann.fooddelivery.deliverservice.dto.DeliveryOrderResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;
import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DeliveryOrderAccessDeniedException;
import de.ullmann.fooddelivery.deliverservice.exception.DeliveryOrderNotFoundException;
import de.ullmann.fooddelivery.deliverservice.repository.DeliveryOrderRepository;
import de.ullmann.fooddelivery.deliverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryService {

    private static final String AGGREGATE_TYPE = "DeliveryOrder";
    private static final String DRIVER_ROLE_AUTHORITY = "ROLE_" + Role.DELIVERY_DRIVER;

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final DriverRepository driverRepository;
    private final OutboxEventService outboxEventService;

    public void receiveOrder(OrderReadyForDeliveryEvent event) {
        DeliveryOrder delivery = DeliveryOrder.create(
                event.orderId(), event.customerId(),
                event.restaurantId(), event.pickupAddress(), event.deliveryAddress());

        driverRepository.findFirstByStatus(DriverStatus.AVAILABLE).ifPresent(driver -> {
            driver.markBusy();
            delivery.assignDriver(driver.getId());
            outboxEventService.createEvent(
                    AGGREGATE_TYPE,
                    delivery.getOrderId(),
                    DriverAssignedEvent.TOPIC,
                    new DriverAssignedEvent(delivery.getOrderId(),
                            delivery.getCustomerId(), LocalDateTime.now()));
        });

        deliveryOrderRepository.save(delivery);
    }

    public void updateStatus(UUID deliveryId, DeliveryStatus newStatus) {
        DeliveryOrder delivery = deliveryOrderRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryOrderNotFoundException(deliveryId));
        assertDriverOwnsDelivery(delivery);

        delivery.transitionTo(newStatus);

        switch (newStatus) {
            case PICKED_UP -> outboxEventService.createEvent(
                    AGGREGATE_TYPE,
                    delivery.getOrderId(),
                    OrderOnTheWayEvent.TOPIC,
                    new OrderOnTheWayEvent(delivery.getOrderId(),
                            delivery.getCustomerId(), LocalDateTime.now()));
            case DELIVERED -> {
                outboxEventService.createEvent(
                        AGGREGATE_TYPE,
                        delivery.getOrderId(),
                        OrderDeliveredEvent.TOPIC,
                        new OrderDeliveredEvent(delivery.getOrderId(),
                                delivery.getCustomerId(), LocalDateTime.now()));
                freeDriver(delivery.getDriverId());
            }
            case CANCELLED -> {
                outboxEventService.createEvent(
                        AGGREGATE_TYPE,
                        delivery.getOrderId(),
                        DeliveryCancelledEvent.TOPIC,
                        new DeliveryCancelledEvent(delivery.getOrderId(),
                                delivery.getCustomerId(), LocalDateTime.now()));
                freeDriver(delivery.getDriverId());
            }
            default -> {
            }
        }
    }

    @Transactional(readOnly = true)
    public DeliveryOrderResponse findById(UUID deliveryId) {
        DeliveryOrder delivery = deliveryOrderRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryOrderNotFoundException(deliveryId));
        assertDriverOwnsDelivery(delivery);
        return DeliveryOrderResponse.from(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryOrderResponse findByOrderId(UUID orderId) {
        DeliveryOrder delivery = deliveryOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryOrderNotFoundException(orderId));
        assertDriverOwnsDelivery(delivery);
        return DeliveryOrderResponse.from(delivery);
    }

    // A DELIVERY_DRIVER caller only ever sees its own assigned deliveries; every other role sees all deliveries.
    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> findAll(DeliveryStatus status) {
        UUID callerDriverId = callerDriverIdIfDriver();
        List<DeliveryOrder> deliveries = callerDriverId != null
                ? (status != null
                        ? deliveryOrderRepository.findAllByDriverIdAndStatus(callerDriverId, status)
                        : deliveryOrderRepository.findAllByDriverId(callerDriverId))
                : (status != null
                        ? deliveryOrderRepository.findAllByStatus(status)
                        : deliveryOrderRepository.findAll());
        return deliveries.stream().map(DeliveryOrderResponse::from).toList();
    }

    private void freeDriver(UUID driverId) {
        if (driverId != null) {
            driverRepository.findById(driverId).ifPresent(Driver::markAvailable);
        }
    }

    // A DELIVERY_DRIVER caller may only access a delivery order assigned to itself.
    // No authentication in context (e.g. plain unit tests, internal callers) is treated as not a driver.
    private void assertDriverOwnsDelivery(DeliveryOrder delivery) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        boolean isDeliveryDriver = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(DRIVER_ROLE_AUTHORITY));
        if (!isDeliveryDriver) {
            return;
        }
        UUID callerId = UUID.fromString((String) authentication.getPrincipal());
        if (!callerId.equals(delivery.getDriverId())) {
            throw new DeliveryOrderAccessDeniedException(delivery.getId());
        }
    }

    // Returns the caller's own id if authenticated as DELIVERY_DRIVER (used to scope findAll to its own
    // deliveries), else null (unrestricted). No authentication in context is treated as not a driver.
    private UUID callerDriverIdIfDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        boolean isDeliveryDriver = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(DRIVER_ROLE_AUTHORITY));
        if (!isDeliveryDriver) {
            return null;
        }
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
