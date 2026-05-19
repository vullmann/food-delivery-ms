package de.ullmann.fooddelivery.restaurantservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;

public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, UUID> {
    List<RestaurantOrder> findAllByRestaurantId(UUID restaurantId);
}