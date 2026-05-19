package de.ullmann.fooddelivery.chatservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderIdStoreTest {

    private OrderIdStore store;

    @BeforeEach
    void setUp() {
        store = new OrderIdStore();
    }

    @Test
    void get_shouldReturnNullForUnknownConversation() {
        assertThat(store.get("unknown")).isNull();
    }

    @Test
    void contains_shouldReturnFalseForUnknownConversation() {
        assertThat(store.contains("unknown")).isFalse();
    }

    @Test
    void captureOrderId_shouldStoreOrderIdWhenConversationIdIsSet() {
        store.setConversationId("conv-1");

        store.captureOrderId("order-abc");

        assertThat(store.get("conv-1")).isEqualTo("order-abc");
        assertThat(store.contains("conv-1")).isTrue();
    }

    @Test
    void captureOrderId_shouldNotOverwriteExistingOrderId() {
        store.setConversationId("conv-1");
        store.captureOrderId("first-order");
        store.captureOrderId("second-order");

        assertThat(store.get("conv-1")).isEqualTo("first-order");
    }

    @Test
    void captureOrderId_shouldDoNothingWhenConversationIdIsNotSet() {
        // No setConversationId call → ThreadLocal is null
        store.captureOrderId("order-xyz");

        assertThat(store.get("order-xyz")).isNull();
    }

    @Test
    void clearConversationId_shouldPreventFurtherCapture() {
        store.setConversationId("conv-2");
        store.clearConversationId();

        store.captureOrderId("some-order");

        assertThat(store.get("conv-2")).isNull();
    }

    @Test
    void setAndClear_shouldWorkInSequence() {
        store.setConversationId("conv-3");
        store.captureOrderId("order-1");
        store.clearConversationId();

        assertThat(store.get("conv-3")).isEqualTo("order-1");
        assertThat(store.contains("conv-3")).isTrue();
    }
}
