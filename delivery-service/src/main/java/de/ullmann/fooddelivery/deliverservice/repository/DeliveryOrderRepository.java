package de.ullmann.fooddelivery.deliverservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.ullmann.fooddelivery.deliverservice.entity.DeliveryOrder;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {
    Optional<DeliveryOrder> findByOrderId(UUID orderId);
}
