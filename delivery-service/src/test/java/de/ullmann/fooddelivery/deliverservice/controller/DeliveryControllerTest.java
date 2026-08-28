package de.ullmann.fooddelivery.deliverservice.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.deliverservice.dto.DeliveryOrderResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DeliveryStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DeliveryOrderNotFoundException;
import de.ullmann.fooddelivery.deliverservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.deliverservice.service.DeliveryService;

// addFilters=false: this slice doesn't load the app's SecurityConfig, so without this the default
// Spring Security auto-configuration would require authentication on every request.
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {DeliveryController.class, GlobalExceptionHandler.class})
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UUID deliveryId;
    private UUID orderId;
    private DeliveryOrderResponse response;

    @BeforeEach
    void setUp() {
        deliveryId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        Address pickupAddress = Address.of("Restaurant St", "5", "Berlin", "10119", "Germany");
        Address deliveryAddress = Address.of("Main St", "1", "Berlin", "10115", "Germany");
        response = new DeliveryOrderResponse(
                deliveryId,
                orderId,
                UUID.randomUUID(),
                null,
                pickupAddress,
                deliveryAddress,
                DeliveryStatus.PENDING,
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC));
    }

    // ── GET /deliveries/{id} ──────────────────────────────────────────────────

    @Test
    void getById_shouldReturn200WithDelivery() throws Exception {
        when(deliveryService.findById(deliveryId)).thenReturn(response);

        mockMvc.perform(get("/deliveries/{id}", deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deliveryId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(deliveryService.findById(deliveryId)).thenThrow(new DeliveryOrderNotFoundException(deliveryId));

        mockMvc.perform(get("/deliveries/{id}", deliveryId))
                .andExpect(status().isNotFound());
    }

    // ── GET /deliveries?orderId= ──────────────────────────────────────────────

    @Test
    void getByOrderId_shouldReturn200WithDelivery() throws Exception {
        when(deliveryService.findByOrderId(orderId)).thenReturn(response);

        mockMvc.perform(get("/deliveries").param("orderId", orderId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void getByOrderId_shouldReturn404_whenNotFound() throws Exception {
        when(deliveryService.findByOrderId(orderId)).thenThrow(new DeliveryOrderNotFoundException(orderId));

        mockMvc.perform(get("/deliveries").param("orderId", orderId.toString()))
                .andExpect(status().isNotFound());
    }

    // ── GET /deliveries ───────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturn200WithAllDeliveries() throws Exception {
        when(deliveryService.findAll(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/deliveries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(deliveryId.toString()));
    }

    @Test
    void getAll_withStatusFilter_shouldReturn200WithFilteredDeliveries() throws Exception {
        when(deliveryService.findAll(DeliveryStatus.PENDING)).thenReturn(List.of(response));

        mockMvc.perform(get("/deliveries").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    // ── PATCH /deliveries/{id}/status ─────────────────────────────────────────

    @Test
    void updateStatus_shouldReturn204() throws Exception {
        doNothing().when(deliveryService).updateStatus(deliveryId, DeliveryStatus.PICKED_UP);

        mockMvc.perform(patch("/deliveries/{id}/status", deliveryId)
                        .param("status", "PICKED_UP"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new DeliveryOrderNotFoundException(deliveryId))
                .when(deliveryService).updateStatus(deliveryId, DeliveryStatus.PICKED_UP);

        mockMvc.perform(patch("/deliveries/{id}/status", deliveryId)
                        .param("status", "PICKED_UP"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_shouldReturn409_whenInvalidTransition() throws Exception {
        doThrow(new IllegalStateException("Cannot transition from PENDING to DELIVERED"))
                .when(deliveryService).updateStatus(deliveryId, DeliveryStatus.DELIVERED);

        mockMvc.perform(patch("/deliveries/{id}/status", deliveryId)
                        .param("status", "DELIVERED"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusParamMissing() throws Exception {
        mockMvc.perform(patch("/deliveries/{id}/status", deliveryId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusParamInvalid() throws Exception {
        mockMvc.perform(patch("/deliveries/{id}/status", deliveryId)
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}
