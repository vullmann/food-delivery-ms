package de.ullmann.fooddelivery.restaurantservice.service;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.dto.*;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItem;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import de.ullmann.fooddelivery.restaurantservice.exception.MenuItemNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.repository.MenuItemRepository;
import de.ullmann.fooddelivery.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    // ── Restaurant ────────────────────────────────────────────────────────────

    @Transactional
    public RestaurantResponse create(CreateRestaurantRequest request) {
        restaurantRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new IllegalArgumentException("A restaurant with email '" + request.email() + "' already exists");
        });

        Address address = Address.of(
                        request.address().street(),
                        request.address().houseNumber(),
                        request.address().city(),
                        request.address().zip(),
                        request.address().country()
        );

        Restaurant restaurant = Restaurant.create(
                        request.name(),
                        request.description(),
                        address,
                        request.phone(),
                        request.email(),
                        request.cuisineType(),
                        request.isOpen()
        );

        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse findById(UUID id) {
        return restaurantRepository.findById(id)
                        .map(RestaurantResponse::from)
                        .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> findAll(CuisineType cuisineType, Boolean isOpen) {
        if (cuisineType != null) {
            return restaurantRepository.findAllByCuisineType(cuisineType).stream()
                            .map(RestaurantResponse::from)
                            .toList();
        }
        if (isOpen != null) {
            return restaurantRepository.findAllByIsOpen(isOpen).stream()
                            .map(RestaurantResponse::from)
                            .toList();
        }
        return restaurantRepository.findAll().stream()
                        .map(RestaurantResponse::from)
                        .toList();
    }

    @Transactional
    public RestaurantResponse update(UUID id, UpdateRestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                        .orElseThrow(() -> new RestaurantNotFoundException(id));

        if (!restaurant.getEmail().equals(request.email())) {
            restaurantRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new IllegalArgumentException("A restaurant with email '" + request.email() + "' already exists");
            });
        }

        Address address = Address.of(
                        request.address().street(),
                        request.address().houseNumber(),
                        request.address().city(),
                        request.address().zip(),
                        request.address().country()
        );

        restaurant.update(
                        request.name(),
                        request.description(),
                        address,
                        request.phone(),
                        request.email(),
                        request.cuisineType(),
                        request.isOpen()
        );

        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public void delete(UUID id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RestaurantNotFoundException(id);
        }
        restaurantRepository.deleteById(id);
    }

    // ── MenuItem ──────────────────────────────────────────────────────────────

    @Transactional
    public MenuItemResponse createMenuItem(UUID restaurantId, CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        MenuItem item = MenuItem.create(
                        restaurant,
                        request.name(),
                        request.description(),
                        request.price(),
                        request.category(),
                        request.available()
        );
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findMenuItems(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findAllByRestaurantId(restaurantId).stream()
                        .map(MenuItemResponse::from)
                        .toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse findMenuItem(UUID restaurantId, UUID itemId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findById(itemId)
                        .map(MenuItemResponse::from)
                        .orElseThrow(() -> new MenuItemNotFoundException(itemId));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(UUID restaurantId, UUID itemId, UpdateMenuItemRequest request) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        MenuItem item = menuItemRepository.findById(itemId)
                        .orElseThrow(() -> new MenuItemNotFoundException(itemId));
        item.update(
                        request.name(),
                        request.description(),
                        request.price(),
                        request.category(),
                        request.available()
        );
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(UUID restaurantId, UUID itemId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        if (!menuItemRepository.existsById(itemId)) {
            throw new MenuItemNotFoundException(itemId);
        }
        menuItemRepository.deleteById(itemId);
    }
}