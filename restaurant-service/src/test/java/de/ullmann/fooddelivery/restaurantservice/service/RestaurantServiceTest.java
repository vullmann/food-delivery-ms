package de.ullmann.fooddelivery.restaurantservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.dto.AddressRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.CreateMenuItemRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.CreateRestaurantRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.MenuItemResponse;
import de.ullmann.fooddelivery.restaurantservice.dto.RestaurantResponse;
import de.ullmann.fooddelivery.restaurantservice.dto.UpdateMenuItemRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.UpdateRestaurantRequest;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItem;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import de.ullmann.fooddelivery.restaurantservice.exception.MenuItemNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.repository.MenuItemRepository;
import de.ullmann.fooddelivery.restaurantservice.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;
    private UUID restaurantId;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        Address address = Address.of("Main St", "1", "Berlin", "10115", "Germany");
        restaurant = Restaurant.create("Pizza Roma", "Best pizza", address,
                "+49123456", "pizza@roma.de", CuisineType.PIZZA, true);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_shouldReturnRestaurantResponse() {
        var request = buildCreateRequest("pizza@roma.de");
        when(restaurantRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        RestaurantResponse response = restaurantService.create(request);

        assertThat(response.name()).isEqualTo("Pizza Roma");
        assertThat(response.email()).isEqualTo("pizza@roma.de");
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void create_shouldThrow_whenEmailAlreadyExists() {
        var request = buildCreateRequest("pizza@roma.de");
        when(restaurantRepository.findByEmail(request.email())).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pizza@roma.de");
        verify(restaurantRepository, never()).save(any());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnRestaurantResponse() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        RestaurantResponse response = restaurantService.findById(restaurantId);

        assertThat(response.name()).isEqualTo("Pizza Roma");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findById(restaurantId))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_withNullFilters_shouldReturnAll() {
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        List<RestaurantResponse> result = restaurantService.findAll(null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_byCuisineType_shouldFilterCorrectly() {
        when(restaurantRepository.findAllByCuisineType(CuisineType.PIZZA)).thenReturn(List.of(restaurant));

        List<RestaurantResponse> result = restaurantService.findAll(CuisineType.PIZZA, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).cuisineType()).isEqualTo(CuisineType.PIZZA);
    }

    @Test
    void findAll_byIsOpen_shouldFilterCorrectly() {
        when(restaurantRepository.findAllByIsOpen(true)).thenReturn(List.of(restaurant));

        List<RestaurantResponse> result = restaurantService.findAll(null, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isOpen()).isTrue();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_shouldReturnUpdatedResponse() {
        var request = buildUpdateRequest("new@roma.de");
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.findByEmail("new@roma.de")).thenReturn(Optional.empty());

        RestaurantResponse response = restaurantService.update(restaurantId, request);

        assertThat(response.email()).isEqualTo("new@roma.de");
        assertThat(response.name()).isEqualTo("Pizza Roma Updated");
    }

    @Test
    void update_shouldThrow_whenRestaurantNotFound() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.update(restaurantId, buildUpdateRequest("x@x.de")))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void update_shouldThrow_whenNewEmailAlreadyTaken() {
        var other = Restaurant.create("Other", null,
                Address.of("A", "1", "B", "12345", "DE"),
                "+49", "taken@mail.de", CuisineType.ASIAN, true);
        var request = buildUpdateRequest("taken@mail.de");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.findByEmail("taken@mail.de")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> restaurantService.update(restaurantId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taken@mail.de");
    }

    @Test
    void update_shouldNotCheckEmail_whenEmailUnchanged() {
        var request = buildUpdateRequest("pizza@roma.de"); // same email
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        RestaurantResponse response = restaurantService.update(restaurantId, request);

        assertThat(response.name()).isEqualTo("Pizza Roma Updated");
        verify(restaurantRepository, never()).findByEmail(any());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_shouldDeleteSuccessfully() {
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);

        restaurantService.delete(restaurantId);

        verify(restaurantRepository).deleteById(restaurantId);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.delete(restaurantId))
                .isInstanceOf(RestaurantNotFoundException.class);
        verify(restaurantRepository, never()).deleteById(any());
    }

    // ── createMenuItem ────────────────────────────────────────────────────────

    @Test
    void createMenuItem_shouldReturnMenuItemResponse() {
        var request = new CreateMenuItemRequest("Margherita", "Classic", new BigDecimal("9.90"),
                MenuItemCategory.MAIN, true);
        var item = MenuItem.create(restaurant, "Margherita", "Classic",
                new BigDecimal("9.90"), MenuItemCategory.MAIN, true);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any())).thenReturn(item);

        MenuItemResponse response = restaurantService.createMenuItem(restaurantId, request);

        assertThat(response.name()).isEqualTo("Margherita");
        assertThat(response.category()).isEqualTo(MenuItemCategory.MAIN);
    }

    @Test
    void createMenuItem_shouldThrow_whenRestaurantNotFound() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.createMenuItem(restaurantId,
                new CreateMenuItemRequest("X", null, BigDecimal.ONE, MenuItemCategory.DRINK, true)))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // ── findMenuItems ─────────────────────────────────────────────────────────

    @Test
    void findMenuItems_shouldReturnList() {
        var item = MenuItem.create(restaurant, "Margherita", null,
                new BigDecimal("9.90"), MenuItemCategory.MAIN, true);

        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findAllByRestaurantId(restaurantId)).thenReturn(List.of(item));

        List<MenuItemResponse> result = restaurantService.findMenuItems(restaurantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Margherita");
    }

    @Test
    void findMenuItems_shouldThrow_whenRestaurantNotFound() {
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.findMenuItems(restaurantId))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // ── findMenuItem ──────────────────────────────────────────────────────────

    @Test
    void findMenuItem_shouldReturnResponse() {
        UUID itemId = UUID.randomUUID();
        var item = MenuItem.create(restaurant, "Margherita", null,
                new BigDecimal("9.90"), MenuItemCategory.MAIN, true);

        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        MenuItemResponse response = restaurantService.findMenuItem(restaurantId, itemId);

        assertThat(response.name()).isEqualTo("Margherita");
    }

    @Test
    void findMenuItem_shouldThrow_whenItemNotFound() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findMenuItem(restaurantId, itemId))
                .isInstanceOf(MenuItemNotFoundException.class);
    }

    @Test
    void findMenuItem_shouldThrow_whenRestaurantNotFound() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.findMenuItem(restaurantId, itemId))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // ── updateMenuItem ────────────────────────────────────────────────────────

    @Test
    void updateMenuItem_shouldReturnUpdatedResponse() {
        UUID itemId = UUID.randomUUID();
        var item = MenuItem.create(restaurant, "Old Name", null,
                new BigDecimal("9.90"), MenuItemCategory.MAIN, true);
        var request = new UpdateMenuItemRequest("New Name", null,
                new BigDecimal("12.00"), MenuItemCategory.STARTER, false);

        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(menuItemRepository.save(any())).thenReturn(item);

        MenuItemResponse response = restaurantService.updateMenuItem(restaurantId, itemId, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.category()).isEqualTo(MenuItemCategory.STARTER);
        assertThat(response.available()).isFalse();
    }

    @Test
    void updateMenuItem_shouldThrow_whenItemNotFound() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.updateMenuItem(restaurantId, itemId,
                new UpdateMenuItemRequest("X", null, BigDecimal.ONE, MenuItemCategory.DRINK, true)))
                .isInstanceOf(MenuItemNotFoundException.class);
    }

    @Test
    void updateMenuItem_shouldThrow_whenRestaurantNotFound() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.updateMenuItem(restaurantId, itemId,
                new UpdateMenuItemRequest("X", null, BigDecimal.ONE, MenuItemCategory.DRINK, true)))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // ── deleteMenuItem ────────────────────────────────────────────────────────

    @Test
    void deleteMenuItem_shouldDeleteSuccessfully() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.existsById(itemId)).thenReturn(true);

        restaurantService.deleteMenuItem(restaurantId, itemId);

        verify(menuItemRepository).deleteById(itemId);
    }

    @Test
    void deleteMenuItem_shouldThrow_whenItemNotFound() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.existsById(itemId)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.deleteMenuItem(restaurantId, itemId))
                .isInstanceOf(MenuItemNotFoundException.class);
        verify(menuItemRepository, never()).deleteById(any());
    }

    @Test
    void deleteMenuItem_shouldThrow_whenRestaurantNotFound() {
        UUID itemId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.deleteMenuItem(restaurantId, itemId))
                .isInstanceOf(RestaurantNotFoundException.class);
        verify(menuItemRepository, never()).deleteById(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateRestaurantRequest buildCreateRequest(String email) {
        return new CreateRestaurantRequest(
                "Pizza Roma", "Best pizza",
                new AddressRequest("Main St", "1", "Berlin", "10115", "Germany"),
                "+49123456", email, CuisineType.PIZZA, true);
    }

    private UpdateRestaurantRequest buildUpdateRequest(String email) {
        return new UpdateRestaurantRequest(
                "Pizza Roma Updated", "Even better pizza",
                new AddressRequest("New St", "2", "Munich", "80331", "Germany"),
                "+49654321", email, CuisineType.ITALIAN, false);
    }
}