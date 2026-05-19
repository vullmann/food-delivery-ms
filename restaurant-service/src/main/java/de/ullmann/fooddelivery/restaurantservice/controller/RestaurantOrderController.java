package de.ullmann.fooddelivery.restaurantservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderStatus;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
public class RestaurantOrderController {

    private final RestaurantOrderService restaurantOrderService;

    @GetMapping
    public ResponseEntity<List<RestaurantOrder>> getOrders(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(restaurantOrderService.findByRestaurant(restaurantId));
    }

    @PatchMapping("/{restaurantOrderId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID restaurantOrderId,
            @RequestParam RestaurantOrderStatus status) {
        restaurantOrderService.updateStatus(restaurantId, restaurantOrderId, status);
        return ResponseEntity.noContent().build();
    }
}