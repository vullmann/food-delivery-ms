package de.ullmann.fooddelivery.restaurantservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.dto.*;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RestaurantController.class, GlobalExceptionHandler.class})
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UUID restaurantId;
    private RestaurantResponse restaurantResponse;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        Address address = Address.of("Main St", "1", "Berlin", "10115", "Germany");
        restaurantResponse = new RestaurantResponse(restaurantId, "Pizza Roma", "Best pizza",
                        address, "+49123456", "pizza@roma.de", CuisineType.PIZZA, true, LocalDateTime.now());
    }

    // ── POST /restaurants ─────────────────────────────────────────────────────

    @Test
    void create_shouldReturn201() throws Exception {
        when(restaurantService.create(any())).thenReturn(restaurantResponse);

        mockMvc.perform(post("/restaurants")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(buildCreateRequest("pizza@roma.de"))))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("Pizza Roma"))
                        .andExpect(jsonPath("$.email").value("pizza@roma.de"));
    }

    @Test
    void create_shouldReturn400_whenNameBlank() throws Exception {
        var request = new CreateRestaurantRequest("", null,
                        new AddressRequest("Main St", "1", "Berlin", "10115", "Germany"),
                        "+49", "x@x.de", CuisineType.PIZZA, true);

        mockMvc.perform(post("/restaurants")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenEmailInvalid() throws Exception {
        var request = new CreateRestaurantRequest("Name", null,
                        new AddressRequest("Main St", "1", "Berlin", "10115", "Germany"),
                        "+49", "not-an-email", CuisineType.PIZZA, true);

        mockMvc.perform(post("/restaurants")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
    }

    // ── GET /restaurants/{id} ─────────────────────────────────────────────────

    @Test
    void findById_shouldReturn200() throws Exception {
        when(restaurantService.findById(restaurantId)).thenReturn(restaurantResponse);

        mockMvc.perform(get("/restaurants/{id}", restaurantId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(restaurantId.toString()));
    }

    @Test
    void findById_shouldReturn404_whenNotFound() throws Exception {
        when(restaurantService.findById(restaurantId))
                        .thenThrow(new RestaurantNotFoundException(restaurantId));

        mockMvc.perform(get("/restaurants/{id}", restaurantId))
                        .andExpect(status().isNotFound());
    }

    // ── GET /restaurants ──────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturn200() throws Exception {
        when(restaurantService.findAll(null, null)).thenReturn(List.of(restaurantResponse));

        mockMvc.perform(get("/restaurants"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value("Pizza Roma"));
    }

    @Test
    void findAll_byCuisineType_shouldReturn200() throws Exception {
        when(restaurantService.findAll(CuisineType.PIZZA, null)).thenReturn(List.of(restaurantResponse));

        mockMvc.perform(get("/restaurants").param("cuisineType", "PIZZA"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].cuisineType").value("PIZZA"));
    }

    // ── PUT /restaurants/{id} ─────────────────────────────────────────────────

    @Test
    void update_shouldReturn200() throws Exception {
        when(restaurantService.update(eq(restaurantId), any())).thenReturn(restaurantResponse);

        mockMvc.perform(put("/restaurants/{id}", restaurantId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(buildUpdateRequest("pizza@roma.de"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Pizza Roma"));
    }

    @Test
    void update_shouldReturn404_whenNotFound() throws Exception {
        when(restaurantService.update(eq(restaurantId), any()))
                        .thenThrow(new RestaurantNotFoundException(restaurantId));

        mockMvc.perform(put("/restaurants/{id}", restaurantId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(buildUpdateRequest("pizza@roma.de"))))
                        .andExpect(status().isNotFound());
    }

    // ── DELETE /restaurants/{id} ──────────────────────────────────────────────

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(restaurantService).delete(restaurantId);

        mockMvc.perform(delete("/restaurants/{id}", restaurantId))
                        .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new RestaurantNotFoundException(restaurantId))
                        .when(restaurantService).delete(restaurantId);

        mockMvc.perform(delete("/restaurants/{id}", restaurantId))
                        .andExpect(status().isNotFound());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateRestaurantRequest buildCreateRequest(String email) {
        return new CreateRestaurantRequest("Pizza Roma", "Best pizza",
                        new AddressRequest("Main St", "1", "Berlin", "10115", "Germany"),
                        "+49123456", email, CuisineType.PIZZA, true);
    }

    private UpdateRestaurantRequest buildUpdateRequest(String email) {
        return new UpdateRestaurantRequest("Pizza Roma", "Best pizza",
                        new AddressRequest("Main St", "1", "Berlin", "10115", "Germany"),
                        "+49123456", email, CuisineType.PIZZA, true);
    }
}