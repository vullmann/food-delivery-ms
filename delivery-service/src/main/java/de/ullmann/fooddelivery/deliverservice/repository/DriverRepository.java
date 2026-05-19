package de.ullmann.fooddelivery.deliverservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findFirstByStatus(DriverStatus status);
    List<Driver> findAllByStatus(DriverStatus status);
}
