package de.ullmann.fooddelivery.deliverservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.security.Role;
import de.ullmann.fooddelivery.deliverservice.dto.DriverResponse;
import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DriverNotFoundException;
import de.ullmann.fooddelivery.deliverservice.exception.InsufficientRoleException;
import de.ullmann.fooddelivery.deliverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DriverService {

    private static final String SUPER_ADMIN_AUTHORITY = "ROLE_" + Role.SUPER_ADMIN;
    private static final String DELIVERY_ADMIN_AUTHORITY = "ROLE_" + Role.DELIVERY_ADMIN;

    private final DriverRepository driverRepository;

    // Consumes UserRegisteredEvent (role=DELIVERY_DRIVER) so the driver profile shares
    // its id with the auth-service userId; idempotent against redelivery.
    public void registerFromEvent(UserRegisteredEvent event) {
        if (driverRepository.existsById(event.userId())) {
            log.info("Driver profile for userId={} already exists, skipping", event.userId());
            return;
        }
        Driver driver = Driver.createWithId(
                event.userId(), event.firstName(), event.lastName(), event.phone());
        driverRepository.save(driver);
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(UUID id) {
        assertCallerManagesDrivers();
        return driverRepository.findById(id)
                .map(DriverResponse::from)
                .orElseThrow(() -> new DriverNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll(DriverStatus status) {
        assertCallerManagesDrivers();
        List<Driver> drivers = status != null
                ? driverRepository.findAllByStatus(status)
                : driverRepository.findAll();
        return drivers.stream().map(DriverResponse::from).toList();
    }

    public void updateStatus(UUID id, DriverStatus newStatus) {
        assertCallerManagesDrivers();
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));
        switch (newStatus) {
            case AVAILABLE -> driver.markAvailable();
            case BUSY      -> driver.markBusy();
            case OFFLINE   -> driver.markOffline();
        }
    }

    public void delete(UUID id) {
        assertCallerManagesDrivers();
        if (!driverRepository.existsById(id)) {
            throw new DriverNotFoundException(id);
        }
        driverRepository.deleteById(id);
    }

    // The driver roster may only be managed by SUPER_ADMIN or DELIVERY_ADMIN. A DELIVERY_DRIVER may only
    // manage its own deliveries (see DeliveryService), not the roster, and no other role has any business
    // reason to view or modify it. No authentication in context (e.g. plain unit tests, internal callers)
    // is treated as unrestricted.
    private void assertCallerManagesDrivers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        boolean allowed = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(SUPER_ADMIN_AUTHORITY)
                        || a.getAuthority().equals(DELIVERY_ADMIN_AUTHORITY));
        if (!allowed) {
            throw new InsufficientRoleException("Only SUPER_ADMIN or DELIVERY_ADMIN may manage the driver roster");
        }
    }
}
