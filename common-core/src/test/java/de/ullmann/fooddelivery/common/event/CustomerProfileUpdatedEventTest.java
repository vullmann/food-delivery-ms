package de.ullmann.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerProfileUpdatedEventTest {

    @Test
    void create_shouldStoreAllFields() {
        UUID customerId = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.now();

        CustomerProfileUpdatedEvent event = new CustomerProfileUpdatedEvent(customerId, "+49123456789", updatedAt);

        assertThat(event.customerId()).isEqualTo(customerId);
        assertThat(event.phone()).isEqualTo("+49123456789");
        assertThat(event.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void topic_shouldBeCustomerProfileUpdated() {
        assertThat(CustomerProfileUpdatedEvent.TOPIC).isEqualTo("customer.profile.updated");
    }

    @Test
    void create_withNullCustomerId_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> new CustomerProfileUpdatedEvent(null, "+49123456789", LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("customerId");
    }

    @Test
    void create_withNullPhone_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> new CustomerProfileUpdatedEvent(UUID.randomUUID(), null, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("phone");
    }

    @Test
    void create_withNullUpdatedAt_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> new CustomerProfileUpdatedEvent(UUID.randomUUID(), "+49123456789", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("updatedAt");
    }
}
