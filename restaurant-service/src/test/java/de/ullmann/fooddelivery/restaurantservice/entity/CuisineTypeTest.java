package de.ullmann.fooddelivery.restaurantservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CuisineTypeTest {

    @Test
    void values_shouldContainAllTypes() {
        assertThat(CuisineType.values()).containsExactlyInAnyOrder(
                CuisineType.ITALIAN, CuisineType.ASIAN, CuisineType.BURGER, CuisineType.PIZZA,
                CuisineType.MEXICAN, CuisineType.INDIAN, CuisineType.GREEK, CuisineType.AMERICAN,
                CuisineType.SUSHI, CuisineType.OTHER
        );
    }

    @Test
    void valueOf_shouldReturnCorrectConstant() {
        assertThat(CuisineType.valueOf("ITALIAN")).isEqualTo(CuisineType.ITALIAN);
        assertThat(CuisineType.valueOf("OTHER")).isEqualTo(CuisineType.OTHER);
    }
}
