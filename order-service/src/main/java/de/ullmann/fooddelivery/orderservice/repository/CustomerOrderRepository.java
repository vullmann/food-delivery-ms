package de.ullmann.fooddelivery.orderservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findAllByCustomerId(UUID customerId);
}