package de.ullmann.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderItemDtoTest {

    private final UUID menuItemId = UUID.randomUUID();
    private final String name = "Pasta Carbonara";
    private final Integer quantity = 2;
    private final BigDecimal price = new BigDecimal("12.50");

    @Test
    void shouldCreateOrderItemWithValidData() {
        OrderItemDto item = new OrderItemDto(menuItemId, name, quantity, price);

        assertThat(item.menuItemId()).isEqualTo(menuItemId);
        assertThat(item.name()).isEqualTo(name);
        assertThat(item.quantity()).isEqualTo(quantity);
        assertThat(item.price()).isEqualByComparingTo(price);
    }

    @Test
    void shouldThrowWhenMenuItemIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItemDto(null, name, quantity, price))
                .withMessage("menuItemId must not be null");
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, null, quantity, price))
                .withMessage("name must not be null");
    }

    @Test
    void shouldThrowWhenQuantityIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, name, null, price))
                .withMessage("quantity must not be null");
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, name, quantity, null))
                .withMessage("price must not be null");
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, "   ", quantity, price))
                .withMessage("name must not be blank");
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, "", quantity, price))
                .withMessage("name must not be blank");
    }

    @Test
    void shouldThrowWhenQuantityIsZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, name, 0, price))
                .withMessage("quantity must be >= 1");
    }

    @Test
    void shouldThrowWhenQuantityIsNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, name, -5, price))
                .withMessage("quantity must be >= 1");
    }

    @Test
    void shouldThrowWhenPriceIsNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItemDto(menuItemId, name, quantity, new BigDecimal("-5.00")))
                .withMessage("price must be >= 0");
    }

    @Test
    void shouldAllowPriceOfZero() {
        OrderItemDto item = new OrderItemDto(menuItemId, name, quantity, BigDecimal.ZERO);

        assertThat(item.price()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldStripTrailingZerosFromPrice() {
        BigDecimal priceWithTrailingZeros = new BigDecimal("12.50000");
        OrderItemDto item = new OrderItemDto(menuItemId, name, quantity, priceWithTrailingZeros);

        assertThat(item.price()).isEqualByComparingTo(new BigDecimal("12.5"));
    }

    @Test
    void shouldSupportRecordEquality() {
        OrderItemDto item1 = new OrderItemDto(menuItemId, name, quantity, price);
        OrderItemDto item2 = new OrderItemDto(menuItemId, name, quantity, price);

        assertThat(item1).isEqualTo(item2).hasSameHashCodeAs(item2);
    }

    @Test
    void shouldSupportRecordToString() {
        OrderItemDto item = new OrderItemDto(menuItemId, name, quantity, price);

        assertThat(item.toString())
                .contains("OrderItemDto")
                .contains(menuItemId.toString())
                .contains(name);
    }
}

