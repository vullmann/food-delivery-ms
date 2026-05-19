package de.ullmann.fooddelivery.restaurantservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.restaurantservice.dto.CreateMenuItemRequest;
import de.ullmann.fooddelivery.restaurantservice.dto.MenuItemResponse;
import de.ullmann.fooddelivery.restaurantservice.dto.UpdateMenuItemRequest;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;
import de.ullmann.fooddelivery.restaurantservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.restaurantservice.exception.MenuItemNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantService;

@WebMvcTest(controllers = {MenuItemController.class, GlobalExceptionHandler.class})
class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UUID restaurantId;
    private UUID itemId;
    private MenuItemResponse menuItemResponse;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        menuItemResponse = new MenuItemResponse(itemId, restaurantId, "Margherita",
                "Classic", new BigDecimal("9.90"), MenuItemCategory.MAIN, true);
    }

    // ── POST /restaurants/{restaurantId}/menu-items ───────────────────────────

    @Test
    void createMenuItem_shouldReturn201() throws Exception {
        when(restaurantService.createMenuItem(eq(restaurantId), any())).thenReturn(menuItemResponse);

        mockMvc.perform(post("/restaurants/{restaurantId}/menu-items", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMenuItemRequest("Margherita", "Classic",
                                        new BigDecimal("9.90"), MenuItemCategory.MAIN, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Margherita"));
    }

    @Test
    void createMenuItem_shouldReturn400_whenNameBlank() throws Exception {
        mockMvc.perform(post("/restaurants/{restaurantId}/menu-items", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMenuItemRequest("", "Classic",
                                        new BigDecimal("9.90"), MenuItemCategory.MAIN, true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMenuItem_shouldReturn400_whenDescriptionBlank() throws Exception {
        mockMvc.perform(post("/restaurants/{restaurantId}/menu-items", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMenuItemRequest("Pasta", "",
                                        new BigDecimal("9.90"), MenuItemCategory.MAIN, true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMenuItem_shouldReturn404_whenRestaurantNotFound() throws Exception {
        when(restaurantService.createMenuItem(eq(restaurantId), any()))
                .thenThrow(new RestaurantNotFoundException(restaurantId));

        mockMvc.perform(post("/restaurants/{restaurantId}/menu-items", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMenuItemRequest("Margherita", "nice and tasty",
                                        new BigDecimal("9.90"), MenuItemCategory.MAIN, true))))
                .andExpect(status().isNotFound());
    }

    // ── GET /restaurants/{restaurantId}/menu-items ────────────────────────────

    @Test
    void getMenuItems_shouldReturn200() throws Exception {
        when(restaurantService.findMenuItems(restaurantId)).thenReturn(List.of(menuItemResponse));

        mockMvc.perform(get("/restaurants/{restaurantId}/menu-items", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Margherita"));
    }

    @Test
    void getMenuItems_shouldReturn404_whenRestaurantNotFound() throws Exception {
        when(restaurantService.findMenuItems(restaurantId))
                .thenThrow(new RestaurantNotFoundException(restaurantId));

        mockMvc.perform(get("/restaurants/{restaurantId}/menu-items", restaurantId))
                .andExpect(status().isNotFound());
    }

    // ── GET /restaurants/{restaurantId}/menu-items/{itemId} ──────────────────

    @Test
    void getMenuItem_shouldReturn200() throws Exception {
        when(restaurantService.findMenuItem(restaurantId, itemId)).thenReturn(menuItemResponse);

        mockMvc.perform(get("/restaurants/{restaurantId}/menu-items/{itemId}", restaurantId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()));
    }

    @Test
    void getMenuItem_shouldReturn404_whenItemNotFound() throws Exception {
        when(restaurantService.findMenuItem(restaurantId, itemId))
                .thenThrow(new MenuItemNotFoundException(itemId));

        mockMvc.perform(get("/restaurants/{restaurantId}/menu-items/{itemId}", restaurantId, itemId))
                .andExpect(status().isNotFound());
    }

    // ── PUT /restaurants/{restaurantId}/menu-items/{itemId} ──────────────────

    @Test
    void updateMenuItem_shouldReturn200() throws Exception {
        when(restaurantService.updateMenuItem(eq(restaurantId), eq(itemId), any()))
                .thenReturn(menuItemResponse);

        mockMvc.perform(put("/restaurants/{restaurantId}/menu-items/{itemId}", restaurantId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMenuItemRequest("Margherita", "Classic",
                                        new BigDecimal("9.90"), MenuItemCategory.MAIN, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Margherita"));
    }

    @Test
    void updateMenuItem_shouldReturn404_whenItemNotFound() throws Exception {
        when(restaurantService.updateMenuItem(eq(restaurantId), eq(itemId), any()))
                .thenThrow(new MenuItemNotFoundException(itemId));

        mockMvc.perform(put("/restaurants/{restaurantId}/menu-items/{itemId}", restaurantId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMenuItemRequest("X", "description",
                                        BigDecimal.ONE, MenuItemCategory.DRINK, false))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /restaurants/{restaurantId}/menu-items/{itemId} ───────────────

    @Test
    void deleteMenuItem_shouldReturn204() throws Exception {
        doNothing().when(restaurantService).deleteMenuItem(restaurantId, itemId);

        mockMvc.perform(delete("/restaurants/{restaurantId}/menu-items/{itemId}", restaurantId, itemId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMenuItem_shouldReturn404_whenItemNotFound() throws Exception {
        doThrow(new MenuItemNotFoundException(itemId))
                .when(restaurantService).deleteMenuItem(restaurantId, itemId);

        mockMvc.perform(delete("/restaurants/{restaurantId}/menu-items/{itemId}", restaurantId, itemId))
                .andExpect(status().isNotFound());
    }
}