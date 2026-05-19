package de.ullmann.fooddelivery.restaurantservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemCategoryTest {

    @Test
    void values_shouldContainAllCategories() {
        assertThat(MenuItemCategory.values()).containsExactlyInAnyOrder(
                MenuItemCategory.STARTER, MenuItemCategory.MAIN,
                MenuItemCategory.DESSERT, MenuItemCategory.DRINK
        );
    }

    @Test
    void valueOf_shouldReturnCorrectConstant() {
        assertThat(MenuItemCategory.valueOf("STARTER")).isEqualTo(MenuItemCategory.STARTER);
        assertThat(MenuItemCategory.valueOf("DRINK")).isEqualTo(MenuItemCategory.DRINK);
    }
}
