package de.ullmann.fooddelivery.restaurantservice.repository;

import de.ullmann.fooddelivery.restaurantservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findAllByRestaurantId(UUID restaurantId);
}