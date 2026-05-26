package de.ullmann.fooddelivery.restaurantservice.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrder;
import de.ullmann.fooddelivery.restaurantservice.entity.RestaurantOrderStatus;
import de.ullmann.fooddelivery.restaurantservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantOrderAccessDeniedException;
import de.ullmann.fooddelivery.restaurantservice.exception.RestaurantOrderNotFoundException;
import de.ullmann.fooddelivery.restaurantservice.service.RestaurantOrderService;

@WebMvcTest(controllers = {RestaurantOrderController.class, GlobalExceptionHandler.class})
class RestaurantOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantOrderService restaurantOrderService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UUID restaurantId;
    private UUID customerOrderId;
    private RestaurantOrder restaurantOrder;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        customerOrderId = UUID.randomUUID();
        Address address = Address.of("Main St", "1", "Berlin", "10115", "Germany");
        restaurantOrder = RestaurantOrder.create(customerOrderId, restaurantId, UUID.randomUUID(), address);
    }

    // ── GET /restaurants/{restaurantId}/orders ────────────────────────────────

    @Test
    void getOrders_shouldReturn200WithList() throws Exception {
        when(restaurantOrderService.findByRestaurant(restaurantId)).thenReturn(List.of(restaurantOrder));

        mockMvc.perform(get("/restaurants/{restaurantId}/orders", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerOrderId").value(customerOrderId.toString()))
                .andExpect(jsonPath("$[0].restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"));
    }

    @Test
    void getOrders_shouldReturn200WithEmptyList() throws Exception {
        when(restaurantOrderService.findByRestaurant(restaurantId)).thenReturn(List.of());

        mockMvc.perform(get("/restaurants/{restaurantId}/orders", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── PATCH /restaurants/{restaurantId}/orders/{orderId}/status ─────────────

    @Test
    void updateStatus_shouldReturn204() throws Exception {
        doNothing().when(restaurantOrderService).updateStatus(restaurantId, customerOrderId, RestaurantOrderStatus.CONFIRMED);

        mockMvc.perform(patch("/restaurants/{restaurantId}/orders/{orderId}/status", restaurantId, customerOrderId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn204_whenStatusIsInPreparation() throws Exception {
        doNothing().when(restaurantOrderService).updateStatus(restaurantId, customerOrderId, RestaurantOrderStatus.PREPARING);

        mockMvc.perform(patch("/restaurants/{restaurantId}/orders/{orderId}/status", restaurantId, customerOrderId)
                        .param("status", "PREPARING"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn404_whenOrderNotFound() throws Exception {
        doThrow(new RestaurantOrderNotFoundException(customerOrderId))
                .when(restaurantOrderService).updateStatus(restaurantId, customerOrderId, RestaurantOrderStatus.CONFIRMED);

        mockMvc.perform(patch("/restaurants/{restaurantId}/orders/{orderId}/status", restaurantId, customerOrderId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_shouldReturn403_whenOrderBelongsToDifferentRestaurant() throws Exception {
        doThrow(new RestaurantOrderAccessDeniedException(customerOrderId, restaurantId))
                .when(restaurantOrderService).updateStatus(restaurantId, customerOrderId, RestaurantOrderStatus.CONFIRMED);

        mockMvc.perform(patch("/restaurants/{restaurantId}/orders/{orderId}/status", restaurantId, customerOrderId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusParamMissing() throws Exception {
        mockMvc.perform(patch("/restaurants/{restaurantId}/orders/{orderId}/status", restaurantId, customerOrderId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusParamInvalid() throws Exception {
        mockMvc.perform(patch("/restaurants/{restaurantId}/orders/{orderId}/status", restaurantId, customerOrderId)
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
}
