package de.ullmann.fooddelivery.notificationservice.entity;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerPhoneProjectionTest {

    @Test
    void of_shouldSetAllFields() {
        UUID customerId = UUID.randomUUID();
        CustomerPhoneProjection projection = CustomerPhoneProjection.of(customerId, "+49123456789");

        assertThat(projection.getCustomerId()).isEqualTo(customerId);
        assertThat(projection.getPhone()).isEqualTo("+49123456789");
    }

    @Test
    void updatePhone_shouldChangePhone() {
        UUID customerId = UUID.randomUUID();
        CustomerPhoneProjection projection = CustomerPhoneProjection.of(customerId, "+49111111111");

        projection.updatePhone("+49999999999");

        assertThat(projection.getPhone()).isEqualTo("+49999999999");
    }
}
