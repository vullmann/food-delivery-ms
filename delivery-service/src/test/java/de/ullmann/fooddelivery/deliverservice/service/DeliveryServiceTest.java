package de.ullmann.fooddelivery.deliverservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.DeliveryCancelledEvent;
import de.ullmann.fooddelivery.common.event.DriverAssignedEvent;
import de.ullmann.fooddelivery.common.event.OrderDeliveredEvent;
import de.ullmann.fooddelivery.common.event.OrderOnTheWayEvent;
import de.ullmann.fooddelivery.common.event.OrderReadyForDeliveryEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.deliverservice.dto.DeliveryOrderResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;
import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DeliveryOrderNotFoundException;
import de.ullmann.fooddelivery.deliverservice.repository.DeliveryOrderRepository;
import de.ullmann.fooddelivery.deliverservice.repository.DriverRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private DeliveryService deliveryService;

    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private Address pickupAddress;
    private Address deliveryAddress;
    private OrderReadyForDeliveryEvent readyEvent;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        pickupAddress = Address.of("Restaurant St", "5", "Berlin", "10119", "Germany");
        deliveryAddress = Address.of("Main St", "1", "Berlin", "10115", "Germany");
        readyEvent = new OrderReadyForDeliveryEvent(orderId, customerId, restaurantId, pickupAddress, deliveryAddress, LocalDateTime.now());
    }

    // ── receiveOrder ──────────────────────────────────────────────────────────

    @Test
    void receiveOrder_withAvailableDriver_shouldAssignAndPublishDriverAssignedEvent() {
        Driver driver = Driver.create("Alice", "Smith", "+49111");
        when(driverRepository.findFirstByStatus(DriverStatus.AVAILABLE)).thenReturn(Optional.of(driver));
        ArgumentCaptor<DeliveryOrder> captor = ArgumentCaptor.forClass(DeliveryOrder.class);

        deliveryService.receiveOrder(readyEvent);

        verify(deliveryOrderRepository).save(captor.capture());
        DeliveryOrder saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(DeliveryStatus.DRIVER_ASSIGNED);
        assertThat(saved.getDriverId()).isEqualTo(driver.getId());
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);

        verify(outboxEventService).createEvent(any(), eq(orderId), eq(DriverAssignedEvent.TOPIC), any(DriverAssignedEvent.class));
    }

    @Test
    void receiveOrder_withNoAvailableDriver_shouldSaveAsPending() {
        when(driverRepository.findFirstByStatus(DriverStatus.AVAILABLE)).thenReturn(Optional.empty());
        ArgumentCaptor<DeliveryOrder> captor = ArgumentCaptor.forClass(DeliveryOrder.class);

        deliveryService.receiveOrder(readyEvent);

        verify(deliveryOrderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DeliveryStatus.PENDING);
        verify(outboxEventService, never()).createEvent(any(), any(), any(), any());
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_toPickedUp_shouldPublishOrderOnTheWayEvent() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.assignDriver(UUID.randomUUID());
        when(deliveryOrderRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        deliveryService.updateStatus(delivery.getId(), DeliveryStatus.PICKED_UP);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);
        verify(outboxEventService).createEvent(any(), eq(orderId), eq(OrderOnTheWayEvent.TOPIC), any(OrderOnTheWayEvent.class));
    }

    @Test
    void updateStatus_toDelivered_shouldPublishOrderDeliveredEventAndFreeDriver() {
        Driver driver = Driver.create("Bob", "Jones", "+49222");
        driver.markBusy();
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.assignDriver(driver.getId());
        delivery.transitionTo(DeliveryStatus.PICKED_UP);
        when(deliveryOrderRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        deliveryService.updateStatus(delivery.getId(), DeliveryStatus.DELIVERED);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        verify(outboxEventService).createEvent(any(), eq(orderId), eq(OrderDeliveredEvent.TOPIC), any(OrderDeliveredEvent.class));
    }

    @Test
    void updateStatus_toCancelled_shouldPublishDeliveryCancelledEventAndFreeDriver() {
        Driver driver = Driver.create("Carol", "Brown", "+49333");
        driver.markBusy();
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        delivery.assignDriver(driver.getId());
        when(deliveryOrderRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        deliveryService.updateStatus(delivery.getId(), DeliveryStatus.CANCELLED);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        verify(outboxEventService).createEvent(any(), eq(orderId), eq(DeliveryCancelledEvent.TOPIC), any(DeliveryCancelledEvent.class));
    }

    @Test
    void updateStatus_shouldThrow_whenDeliveryNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(deliveryOrderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.updateStatus(unknownId, DeliveryStatus.PICKED_UP))
                .isInstanceOf(DeliveryOrderNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void updateStatus_shouldThrow_whenInvalidTransition() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        when(deliveryOrderRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        assertThatThrownBy(() -> deliveryService.updateStatus(delivery.getId(), DeliveryStatus.DELIVERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("DELIVERED");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        when(deliveryOrderRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        DeliveryOrderResponse response = deliveryService.findById(delivery.getId());

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(DeliveryStatus.PENDING);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(deliveryOrderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.findById(unknownId))
                .isInstanceOf(DeliveryOrderNotFoundException.class);
    }

    // ── findByOrderId ─────────────────────────────────────────────────────────

    @Test
    void findByOrderId_shouldReturnResponse() {
        DeliveryOrder delivery = DeliveryOrder.create(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
        when(deliveryOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));

        DeliveryOrderResponse response = deliveryService.findByOrderId(orderId);

        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    void findByOrderId_shouldThrow_whenNotFound() {
        when(deliveryOrderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.findByOrderId(orderId))
                .isInstanceOf(DeliveryOrderNotFoundException.class);
    }
}
