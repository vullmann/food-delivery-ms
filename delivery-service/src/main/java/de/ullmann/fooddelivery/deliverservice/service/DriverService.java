package de.ullmann.fooddelivery.deliverservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.deliverservice.dto.CreateDriverRequest;
import de.ullmann.fooddelivery.deliverservice.dto.DriverResponse;
import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DriverNotFoundException;
import de.ullmann.fooddelivery.deliverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverResponse create(CreateDriverRequest request) {
        Driver driver = Driver.create(request.firstName(), request.lastName(), request.phone());
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(UUID id) {
        return driverRepository.findById(id)
                .map(DriverResponse::from)
                .orElseThrow(() -> new DriverNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll(DriverStatus status) {
        List<Driver> drivers = status != null
                ? driverRepository.findAllByStatus(status)
                : driverRepository.findAll();
        return drivers.stream().map(DriverResponse::from).toList();
    }

    public void updateStatus(UUID id, DriverStatus newStatus) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));
        switch (newStatus) {
            case AVAILABLE -> driver.markAvailable();
            case BUSY      -> driver.markBusy();
            case OFFLINE   -> driver.markOffline();
        }
    }

    public void delete(UUID id) {
        if (!driverRepository.existsById(id)) {
            throw new DriverNotFoundException(id);
        }
        driverRepository.deleteById(id);
    }
}
