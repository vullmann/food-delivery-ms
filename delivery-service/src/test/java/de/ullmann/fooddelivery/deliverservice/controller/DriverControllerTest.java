package de.ullmann.fooddelivery.deliverservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import de.ullmann.fooddelivery.deliverservice.dto.CreateDriverRequest;
import de.ullmann.fooddelivery.deliverservice.dto.DriverResponse;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DriverNotFoundException;
import de.ullmann.fooddelivery.deliverservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.deliverservice.service.DriverService;

@WebMvcTest(controllers = {DriverController.class, GlobalExceptionHandler.class})
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverService driverService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UUID driverId;
    private DriverResponse driverResponse;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
        driverResponse = new DriverResponse(driverId, "Max", "Müller", "+49 30 11111111",
                DriverStatus.AVAILABLE, LocalDateTime.now());
    }

    // ── POST /drivers ─────────────────────────────────────────────────────────

    @Test
    void create_shouldReturn201WithDriver() throws Exception {
        when(driverService.create(any(CreateDriverRequest.class))).thenReturn(driverResponse);

        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateDriverRequest("Max", "Müller", "+49 30 11111111"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(driverId.toString()))
                .andExpect(jsonPath("$.firstName").value("Max"))
                .andExpect(jsonPath("$.lastName").value("Müller"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void create_shouldReturn400_whenFirstNameBlank() throws Exception {
        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateDriverRequest("", "Müller", "+49 30 11111111"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenPhoneBlank() throws Exception {
        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateDriverRequest("Max", "Müller", ""))))
                .andExpect(status().isBadRequest());
    }

    // ── GET /drivers/{id} ─────────────────────────────────────────────────────

    @Test
    void getById_shouldReturn200WithDriver() throws Exception {
        when(driverService.findById(driverId)).thenReturn(driverResponse);

        mockMvc.perform(get("/drivers/{id}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(driverId.toString()))
                .andExpect(jsonPath("$.firstName").value("Max"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(driverService.findById(driverId)).thenThrow(new DriverNotFoundException(driverId));

        mockMvc.perform(get("/drivers/{id}", driverId))
                .andExpect(status().isNotFound());
    }

    // ── GET /drivers ──────────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        when(driverService.findAll(null)).thenReturn(List.of(driverResponse));

        mockMvc.perform(get("/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(driverId.toString()));
    }

    @Test
    void getAll_withStatusFilter_shouldReturn200() throws Exception {
        when(driverService.findAll(DriverStatus.AVAILABLE)).thenReturn(List.of(driverResponse));

        mockMvc.perform(get("/drivers").param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void getAll_withInvalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/drivers").param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /drivers/{id}/status ────────────────────────────────────────────

    @Test
    void updateStatus_shouldReturn204() throws Exception {
        doNothing().when(driverService).updateStatus(driverId, DriverStatus.OFFLINE);

        mockMvc.perform(patch("/drivers/{id}/status", driverId)
                        .param("status", "OFFLINE"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new DriverNotFoundException(driverId))
                .when(driverService).updateStatus(driverId, DriverStatus.OFFLINE);

        mockMvc.perform(patch("/drivers/{id}/status", driverId)
                        .param("status", "OFFLINE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusParamMissing() throws Exception {
        mockMvc.perform(patch("/drivers/{id}/status", driverId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusParamInvalid() throws Exception {
        mockMvc.perform(patch("/drivers/{id}/status", driverId)
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /drivers/{id} ──────────────────────────────────────────────────

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(driverService).delete(driverId);

        mockMvc.perform(delete("/drivers/{id}", driverId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new DriverNotFoundException(driverId))
                .when(driverService).delete(driverId);

        mockMvc.perform(delete("/drivers/{id}", driverId))
                .andExpect(status().isNotFound());
    }
}
