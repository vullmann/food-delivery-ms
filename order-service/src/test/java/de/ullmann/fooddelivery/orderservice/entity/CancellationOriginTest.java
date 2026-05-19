package de.ullmann.fooddelivery.orderservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CancellationOriginTest {

    @Test
    void values_shouldContainAllOrigins() {
        CancellationOrigin[] values = CancellationOrigin.values();
        assertThat(values).containsExactlyInAnyOrder(
                CancellationOrigin.CUSTOMER,
                CancellationOrigin.ORDER_SERVICE,
                CancellationOrigin.RESTAURANT_SERVICE,
                CancellationOrigin.DELIVERY_SERVICE
        );
    }

    @Test
    void valueOf_shouldReturnCorrectConstant() {
        assertThat(CancellationOrigin.valueOf("CUSTOMER")).isEqualTo(CancellationOrigin.CUSTOMER);
        assertThat(CancellationOrigin.valueOf("ORDER_SERVICE")).isEqualTo(CancellationOrigin.ORDER_SERVICE);
        assertThat(CancellationOrigin.valueOf("RESTAURANT_SERVICE")).isEqualTo(CancellationOrigin.RESTAURANT_SERVICE);
        assertThat(CancellationOrigin.valueOf("DELIVERY_SERVICE")).isEqualTo(CancellationOrigin.DELIVERY_SERVICE);
    }
}
