package de.ullmann.fooddelivery.restaurantservice.entity;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemTest {

    private Restaurant buildRestaurant() {
        return Restaurant.create("Trattoria", "Italian food",
                Address.of("Via Roma", "1", "Rome", "00100", "Italy"),
                "+39123", "info@trattoria.it", CuisineType.ITALIAN, true);
    }

    @Test
    void create_shouldSetAllFields() {
        Restaurant restaurant = buildRestaurant();
        MenuItem item = MenuItem.create(restaurant, "Pasta", "Carbonara",
                BigDecimal.valueOf(12.50), MenuItemCategory.MAIN, true);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getRestaurant()).isSameAs(restaurant);
        assertThat(item.getName()).isEqualTo("Pasta");
        assertThat(item.getDescription()).isEqualTo("Carbonara");
        assertThat(item.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(12.50));
        assertThat(item.getCategory()).isEqualTo(MenuItemCategory.MAIN);
        assertThat(item.isAvailable()).isTrue();
    }

    @Test
    void update_shouldChangeFields() {
        Restaurant restaurant = buildRestaurant();
        MenuItem item = MenuItem.create(restaurant, "Pasta", "Carbonara",
                BigDecimal.valueOf(12.50), MenuItemCategory.MAIN, true);

        item.update("Pizza", "Margherita", BigDecimal.valueOf(9.00), MenuItemCategory.MAIN, false);

        assertThat(item.getName()).isEqualTo("Pizza");
        assertThat(item.getDescription()).isEqualTo("Margherita");
        assertThat(item.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(9.00));
        assertThat(item.isAvailable()).isFalse();
    }
}
