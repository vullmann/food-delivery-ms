package de.ullmann.fooddelivery.deliverservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverStatusTest {

    @Test
    void values_shouldContainAllStatuses() {
        assertThat(DriverStatus.values()).containsExactlyInAnyOrder(
                DriverStatus.AVAILABLE,
                DriverStatus.BUSY,
                DriverStatus.OFFLINE
        );
    }

    @Test
    void valueOf_shouldReturnCorrectConstant() {
        assertThat(DriverStatus.valueOf("AVAILABLE")).isEqualTo(DriverStatus.AVAILABLE);
        assertThat(DriverStatus.valueOf("BUSY")).isEqualTo(DriverStatus.BUSY);
        assertThat(DriverStatus.valueOf("OFFLINE")).isEqualTo(DriverStatus.OFFLINE);
    }
}
