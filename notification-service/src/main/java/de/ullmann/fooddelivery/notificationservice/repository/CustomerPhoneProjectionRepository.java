package de.ullmann.fooddelivery.notificationservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.ullmann.fooddelivery.notificationservice.entity.CustomerPhoneProjection;

public interface CustomerPhoneProjectionRepository extends JpaRepository<CustomerPhoneProjection, UUID> {
}
