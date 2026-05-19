package de.ullmann.fooddelivery.restaurantservice.controller;

import de.ullmann.fooddelivery.restaurantservice.dto.CreateRestaurantRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.RestaurantResponse;
import de.ullmann.fooddelivery.restaurantservice.dto.UpdateRestaurantRequest;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse response = restaurantService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> findAll(
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestParam(required = false) Boolean isOpen
    ) {
        return ResponseEntity.ok(restaurantService.findAll(cuisineType, isOpen));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRestaurantRequest request
    ) {
        return ResponseEntity.ok(restaurantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        restaurantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}