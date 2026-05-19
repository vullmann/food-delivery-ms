package de.ullmann.fooddelivery.restaurantservice.dto;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;

import static org.assertj.core.api.Assertions.assertThat;

class CreateMenuItemRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        CreateMenuItemRequest request = new CreateMenuItemRequest(
                "Tiramisu", "Classic Italian dessert", BigDecimal.valueOf(6.50), MenuItemCategory.DESSERT, true);

        assertThat(request.name()).isEqualTo("Tiramisu");
        assertThat(request.description()).isEqualTo("Classic Italian dessert");
        assertThat(request.price()).isEqualByComparingTo(BigDecimal.valueOf(6.50));
        assertThat(request.category()).isEqualTo(MenuItemCategory.DESSERT);
        assertThat(request.available()).isTrue();
    }
}
