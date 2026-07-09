package de.ullmann.fooddelivery.deliverservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.deliverservice.dto.CreateDriverRequest;
import de.ullmann.fooddelivery.deliverservice.dto.DriverResponse;
import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;
import de.ullmann.fooddelivery.deliverservice.exception.DriverNotFoundException;
import de.ullmann.fooddelivery.deliverservice.repository.DriverRepository;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = Driver.create("Max", "Müller", "+49 30 11111111");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAndReturnResponse() {
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.create(
                new CreateDriverRequest("Max", "Müller", "+49 30 11111111"));

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("Max");
        assertThat(captor.getValue().getLastName()).isEqualTo("Müller");
        assertThat(response.status()).isEqualTo(DriverStatus.AVAILABLE);
    }

    // ── registerFromEvent ─────────────────────────────────────────────────────

    @Test
    void registerFromEvent_shouldCreateDriverWithEventUserId() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId, "DELIVERY_DRIVER", "Max", "Müller", "max@example.com",
                "+49 30 11111111", null, LocalDateTime.now());

        when(driverRepository.existsById(userId)).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(i -> i.getArgument(0));

        driverService.registerFromEvent(event);

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(userId);
        assertThat(captor.getValue().getFirstName()).isEqualTo("Max");
    }

    @Test
    void registerFromEvent_shouldSkipWhenDriverAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId, "DELIVERY_DRIVER", "Max", "Müller", "max@example.com",
                "+49 30 11111111", null, LocalDateTime.now());

        when(driverRepository.existsById(userId)).thenReturn(true);

        driverService.registerFromEvent(event);

        verify(driverRepository, org.mockito.Mockito.never()).save(any());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse() {
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        DriverResponse response = driverService.findById(driver.getId());

        assertThat(response.id()).isEqualTo(driver.getId());
        assertThat(response.firstName()).isEqualTo("Max");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(driverRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.findById(unknownId))
                .isInstanceOf(DriverNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_withNoFilter_shouldReturnAllDrivers() {
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        List<DriverResponse> result = driverService.findAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().firstName()).isEqualTo("Max");
    }

    @Test
    void findAll_withStatusFilter_shouldReturnFilteredDrivers() {
        when(driverRepository.findAllByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of(driver));

        List<DriverResponse> result = driverService.findAll(DriverStatus.AVAILABLE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void findAll_withStatusFilter_shouldReturnEmptyList_whenNoneMatch() {
        when(driverRepository.findAllByStatus(DriverStatus.OFFLINE)).thenReturn(List.of());

        List<DriverResponse> result = driverService.findAll(DriverStatus.OFFLINE);

        assertThat(result).isEmpty();
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_toOffline_shouldMarkDriverOffline() {
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        driverService.updateStatus(driver.getId(), DriverStatus.OFFLINE);

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.OFFLINE);
    }

    @Test
    void updateStatus_toBusy_shouldMarkDriverBusy() {
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        driverService.updateStatus(driver.getId(), DriverStatus.BUSY);

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);
    }

    @Test
    void updateStatus_toAvailable_shouldMarkDriverAvailable() {
        driver.markOffline();
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        driverService.updateStatus(driver.getId(), DriverStatus.AVAILABLE);

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void updateStatus_shouldThrow_whenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(driverRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.updateStatus(unknownId, DriverStatus.OFFLINE))
                .isInstanceOf(DriverNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_shouldDeleteDriver() {
        when(driverRepository.existsById(driver.getId())).thenReturn(true);

        driverService.delete(driver.getId());

        verify(driverRepository).deleteById(driver.getId());
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(driverRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> driverService.delete(unknownId))
                .isInstanceOf(DriverNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }
}
