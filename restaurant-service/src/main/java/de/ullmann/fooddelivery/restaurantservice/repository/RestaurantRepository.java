package de.ullmann.fooddelivery.restaurantservice.repository;

import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findByEmail(String email);

    List<Restaurant> findAllByCuisineType(CuisineType cuisineType);

    List<Restaurant> findAllByIsOpen(boolean isOpen);
}