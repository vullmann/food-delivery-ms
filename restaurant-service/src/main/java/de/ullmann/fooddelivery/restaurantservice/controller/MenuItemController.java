package de.ullmann.fooddelivery.restaurantservice.controller;

import de.ullmann.fooddelivery.restaurantservice.dto.CreateMenuItemRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.MenuItemResponse;
import de.ullmann.fooddelivery.restaurantservice.dto.UpdateMenuItemRequest;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menu-items")
public class MenuItemController {

    private final RestaurantService restaurantService;

    public MenuItemController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(@PathVariable UUID restaurantId,
                                           @RequestBody @Valid CreateMenuItemRequest req) {
        return restaurantService.createMenuItem(restaurantId, req);
    }

    @GetMapping
    public List<MenuItemResponse> getMenuItems(@PathVariable UUID restaurantId) {
        return restaurantService.findMenuItems(restaurantId);
    }

    @GetMapping("/{itemId}")
    public MenuItemResponse getMenuItem(@PathVariable UUID restaurantId,
                                        @PathVariable UUID itemId) {
        return restaurantService.findMenuItem(restaurantId, itemId);
    }

    @PutMapping("/{itemId}")
    public MenuItemResponse updateMenuItem(@PathVariable UUID restaurantId,
                                           @PathVariable UUID itemId,
                                           @RequestBody @Valid UpdateMenuItemRequest req) {
        return restaurantService.updateMenuItem(restaurantId, itemId, req);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable UUID restaurantId,
                               @PathVariable UUID itemId) {
        restaurantService.deleteMenuItem(restaurantId, itemId);
    }
}