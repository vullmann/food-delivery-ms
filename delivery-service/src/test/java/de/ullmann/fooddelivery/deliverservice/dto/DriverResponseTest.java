package de.ullmann.fooddelivery.deliverservice.dto;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.deliverservice.entity.Driver;
import de.ullmann.fooddelivery.deliverservice.entity.DriverStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DriverResponseTest {

    @Test
    void from_shouldMapAllFields() {
        Driver driver = Driver.create("Jane", "Smith", "+49987654321");
        DriverResponse response = DriverResponse.from(driver);

        assertThat(response.id()).isNotNull();
        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.lastName()).isEqualTo("Smith");
        assertThat(response.phone()).isEqualTo("+49987654321");
        assertThat(response.status()).isEqualTo(DriverStatus.AVAILABLE);
    }
}
