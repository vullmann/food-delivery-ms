package de.ullmann.fooddelivery.deliverservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.ullmann.fooddelivery.deliverservice.dto.DeliveryOrderResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;
import de.ullmann.fooddelivery.deliverservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryOrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.findById(id));
    }

    @GetMapping(params = "orderId")
    public ResponseEntity<DeliveryOrderResponse> getByOrderId(@RequestParam UUID orderId) {
        return ResponseEntity.ok(deliveryService.findByOrderId(orderId));
    }

    @GetMapping
    public ResponseEntity<List<DeliveryOrderResponse>> getAll(
            @RequestParam(required = false) DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.findAll(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam DeliveryStatus status) {
        deliveryService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
