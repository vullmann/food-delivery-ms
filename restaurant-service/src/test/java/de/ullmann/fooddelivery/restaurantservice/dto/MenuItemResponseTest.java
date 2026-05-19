package de.ullmann.fooddelivery.restaurantservice.dto;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItem;
import de.ullmann.fooddelivery.restaurantservice.entity.MenuItemCategory;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemResponseTest {

    @Test
    void from_shouldMapAllFields() {
        Restaurant restaurant = Restaurant.create("Trattoria", "Italian",
                Address.of("Via Roma", "1", "Rome", "00100", "Italy"),
                "+39123", "info@trattoria.it", CuisineType.ITALIAN, true);
        MenuItem item = MenuItem.create(restaurant, "Pasta", "Carbonara",
                BigDecimal.valueOf(12.50), MenuItemCategory.MAIN, true);

        MenuItemResponse response = MenuItemResponse.from(item);

        assertThat(response.id()).isEqualTo(item.getId());
        assertThat(response.restaurantId()).isEqualTo(restaurant.getId());
        assertThat(response.name()).isEqualTo("Pasta");
        assertThat(response.description()).isEqualTo("Carbonara");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(12.50));
        assertThat(response.category()).isEqualTo(MenuItemCategory.MAIN);
        assertThat(response.available()).isTrue();
    }
}
