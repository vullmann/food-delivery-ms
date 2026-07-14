package de.ullmann.fooddelivery.deliverservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.ullmann.fooddelivery.deliverservice.dto.DriverResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.service.DriverService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(driverService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAll(
            @RequestParam(required = false) DriverStatus status) {
        return ResponseEntity.ok(driverService.findAll(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam DriverStatus status) {
        driverService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
