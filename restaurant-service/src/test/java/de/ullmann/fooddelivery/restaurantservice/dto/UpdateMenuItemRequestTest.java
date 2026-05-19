package de.ullmann.fooddelivery.restaurantservice.dto;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateMenuItemRequestTest {

    @Test
    void constructor_shouldSetAllFields() {
        UpdateMenuItemRequest request = new UpdateMenuItemRequest(
                "Espresso", "Double shot", BigDecimal.valueOf(2.50), MenuItemCategory.DRINK, true);

        assertThat(request.name()).isEqualTo("Espresso");
        assertThat(request.description()).isEqualTo("Double shot");
        assertThat(request.price()).isEqualByComparingTo(BigDecimal.valueOf(2.50));
        assertThat(request.category()).isEqualTo(MenuItemCategory.DRINK);
        assertThat(request.available()).isTrue();
    }
}
