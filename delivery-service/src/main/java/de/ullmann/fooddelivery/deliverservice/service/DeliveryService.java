package de.ullmann.fooddelivery.deliverservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.deliverservice.dto.DeliveryOrderResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;
import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DeliveryOrderNotFoundException;
import de.ullmann.fooddelivery.deliverservice.repository.DeliveryOrderRepository;
import de.ullmann.fooddelivery.deliverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryService {

    private static final String AGGREGATE_TYPE = "DeliveryOrder";

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
        return deliveryOrderRepository.findById(deliveryId)
                .map(DeliveryOrderResponse::from)
                .orElseThrow(() -> new DeliveryOrderNotFoundException(deliveryId));
    }

    @Transactional(readOnly = true)
    public DeliveryOrderResponse findByOrderId(UUID orderId) {
        return deliveryOrderRepository.findByOrderId(orderId)
                .map(DeliveryOrderResponse::from)
                .orElseThrow(() -> new DeliveryOrderNotFoundException(orderId));
    }

    private void freeDriver(UUID driverId) {
        if (driverId != null) {
            driverRepository.findById(driverId).ifPresent(Driver::markAvailable);
        }
    }
}
