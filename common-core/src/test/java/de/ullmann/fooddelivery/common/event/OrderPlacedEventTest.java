package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

class OrderPlacedEventTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final BigDecimal totalAmount = new BigDecimal("45.50");
    private final Address deliveryAddress = Address.of("Musterstraße 1", "23", "Halle", "06108", "DE");
    private final LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    private OrderPlacedEvent createValidEvent() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        return new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount, List.of(item),
                deliveryAddress, createdAt);
    }

    @Test
    void shouldCreateEventWithValidData() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount,
                List.of(item), deliveryAddress, createdAt);

        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.customerId()).isEqualTo(customerId);
        assertThat(event.restaurantId()).isEqualTo(restaurantId);
        assertThat(event.totalAmount()).isEqualByComparingTo(totalAmount);
        assertThat(event.items()).hasSize(1);
        assertThat(event.deliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(event.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new OrderPlacedEvent(null, customerId, restaurantId, totalAmount, List.of(item),
                                deliveryAddress, createdAt))
                .withMessage("orderId must not be null");
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderPlacedEvent(orderId, null, restaurantId, totalAmount, List.of(item),
                        deliveryAddress, createdAt))
                .withMessage("customerId must not be null");
    }

    @Test
    void shouldThrowWhenRestaurantIdIsNull() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderPlacedEvent(orderId, customerId, null, totalAmount, List.of(item),
                        deliveryAddress, createdAt))
                .withMessage("restaurantId must not be null");
    }

    @Test
    void shouldThrowWhenTotalAmountIsNull() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderPlacedEvent(orderId, customerId, restaurantId, null, List.of(item),
                        deliveryAddress, createdAt))
                .withMessage("totalAmount must not be null");
    }

    @Test
    void shouldThrowWhenItemsIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount, null,
                        deliveryAddress, createdAt))
                .withMessage("items must not be null");
    }

    @Test
    void shouldThrowWhenDeliveryAddressIsNull() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount, List.of(item),
                                null, createdAt))
                .withMessage("deliveryAddress must not be null");
    }

    @Test
    void shouldThrowWhenCreatedAtIsNull() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount, List.of(item),
                                deliveryAddress, null))
                .withMessage("createdAt must not be null");
    }

    @Test
    void shouldThrowWhenTotalAmountIsNegative() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> new OrderPlacedEvent(orderId, customerId, restaurantId, new BigDecimal("-10.00"),
                                List.of(item), deliveryAddress, createdAt))
                .withMessage("totalAmount must not be negative");
    }

    @Test
    void shouldThrowWhenItemsIsEmpty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount, List.of(),
                        deliveryAddress, createdAt))
                .withMessage("items must not be empty");
    }

    @Test
    void shouldAllowZeroTotalAmount() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("0.00"));
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, restaurantId, BigDecimal.ZERO,
                List.of(item), deliveryAddress, createdAt);

        assertThat(event.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldStripTrailingZerosFromTotalAmount() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        BigDecimal amountWithTrailingZeros = new BigDecimal("45.50000");
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, restaurantId, amountWithTrailingZeros,
                List.of(item), deliveryAddress, createdAt);

        assertThat(event.totalAmount()).isEqualByComparingTo(new BigDecimal("45.5"));
    }

    @Test
    void shouldCopyItemsList() {
        OrderItemDto item = new OrderItemDto(UUID.randomUUID(), "Pasta", 2, new BigDecimal("12.50"));
        List<OrderItemDto> originalItems = List.of(item);
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, restaurantId, totalAmount,
                originalItems, deliveryAddress, createdAt);

        assertThat(event.items()).isUnmodifiable();
    }

    @Test
    void shouldSupportRecordToString() {
        OrderPlacedEvent event = createValidEvent();

        assertThat(event.toString())
                .contains("OrderPlacedEvent")
                .contains(orderId.toString());
    }
}
