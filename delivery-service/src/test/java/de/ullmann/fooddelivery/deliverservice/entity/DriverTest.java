package de.ullmann.fooddelivery.deliverservice.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class DriverTest {

    @Test
    void createWithId_shouldUseGivenId() {
        UUID id = UUID.randomUUID();

        Driver driver = Driver.createWithId(id, "Alice", "Smith", "+49123456789");

        assertThat(driver.getId()).isEqualTo(id);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void create_shouldSetDefaultStatusToAvailable() {
        Driver driver = Driver.create("Alice", "Smith", "+49123456789");

        assertThat(driver.getId()).isNotNull();
        assertThat(driver.getFirstName()).isEqualTo("Alice");
        assertThat(driver.getLastName()).isEqualTo("Smith");
        assertThat(driver.getPhone()).isEqualTo("+49123456789");
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void markBusy_shouldChangeStatusToBusy() {
        Driver driver = Driver.create("Alice", "Smith", "+49123456789");

        driver.markBusy();

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);
    }

    @Test
    void markAvailable_shouldChangeStatusToAvailable() {
        Driver driver = Driver.create("Alice", "Smith", "+49123456789");
        driver.markBusy();

        driver.markAvailable();

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }
}
