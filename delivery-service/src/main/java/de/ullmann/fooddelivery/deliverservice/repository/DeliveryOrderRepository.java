package de.ullmann.fooddelivery.deliverservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {
    Optional<DeliveryOrder> findByOrderId(UUID orderId);
    List<DeliveryOrder> findAllByStatus(DeliveryStatus status);
    List<DeliveryOrder> findAllByDriverId(UUID driverId);
    List<DeliveryOrder> findAllByDriverIdAndStatus(UUID driverId, DeliveryStatus status);
}
