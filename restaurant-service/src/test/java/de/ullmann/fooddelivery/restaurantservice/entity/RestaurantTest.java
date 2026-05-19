package de.ullmann.fooddelivery.restaurantservice.entity;

import de.ullmann.fooddelivery.common.model.Address;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantTest {

    @Test
    void create_ShouldCreateRestaurantWithAllFields() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");

        // When
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "A great place to eat",
                address,
                "+49123456789",
                "test@restaurant.com",
                CuisineType.ITALIAN,
                true
        );

        // Then
        assertThat(restaurant).isNotNull();
        assertThat(restaurant.getId()).isNotNull();
        assertThat(restaurant.getName()).isEqualTo("Test Restaurant");
        assertThat(restaurant.getDescription()).isEqualTo("A great place to eat");
        assertThat(restaurant.getAddress()).isEqualTo(address);
        assertThat(restaurant.getPhone()).isEqualTo("+49123456789");
        assertThat(restaurant.getEmail()).isEqualTo("test@restaurant.com");
        assertThat(restaurant.getCuisineType()).isEqualTo(CuisineType.ITALIAN);
        assertThat(restaurant.isOpen()).isTrue();
        assertThat(restaurant.getCreatedAt()).isNotNull();
    }

    @Test
    void create_ShouldCreateClosedRestaurant() {
        // Given
        Address address = Address.of("Side Street", "456", "Munich", "80331", "Germany");

        // When
        Restaurant restaurant = Restaurant.create(
                "Closed Restaurant",
                "Currently closed",
                address,
                "+49987654321",
                "closed@restaurant.com",
                CuisineType.ASIAN,
                false
        );

        // Then
        assertThat(restaurant.isOpen()).isFalse();
    }

    @Test
    void update_ShouldUpdateAllFields() {
        // Given
        Address originalAddress = Address.of("Main Street", "123", "Berlin", "10115", "Germany");
        Restaurant restaurant = Restaurant.create(
                "Original Restaurant",
                "Original description",
                originalAddress,
                "+49123456789",
                "original@restaurant.com",
                CuisineType.ITALIAN,
                true
        );

        Address newAddress = Address.of("New Street", "789", "Hamburg", "20095", "Germany");

        // When
        restaurant.update(
                "Updated Restaurant",
                "Updated description",
                newAddress,
                "+49111222333",
                "updated@restaurant.com",
                CuisineType.PIZZA,
                false
        );

        // Then
        assertThat(restaurant.getName()).isEqualTo("Updated Restaurant");
        assertThat(restaurant.getDescription()).isEqualTo("Updated description");
        assertThat(restaurant.getAddress()).isEqualTo(newAddress);
        assertThat(restaurant.getPhone()).isEqualTo("+49111222333");
        assertThat(restaurant.getEmail()).isEqualTo("updated@restaurant.com");
        assertThat(restaurant.getCuisineType()).isEqualTo(CuisineType.PIZZA);
        assertThat(restaurant.isOpen()).isFalse();
    }

    @Test
    void update_ShouldPreserveIdAndCreatedAt() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "Description",
                address,
                "+49123456789",
                "test@restaurant.com",
                CuisineType.ITALIAN,
                true
        );

        var originalId = restaurant.getId();
        var originalCreatedAt = restaurant.getCreatedAt();

        // When
        restaurant.update(
                "Updated Name",
                "Updated description",
                address,
                "+49111222333",
                "updated@restaurant.com",
                CuisineType.ASIAN,
                false
        );

        // Then
        assertThat(restaurant.getId()).isEqualTo(originalId);
        assertThat(restaurant.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void create_ShouldGenerateUniqueIds() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");

        // When
        Restaurant restaurant1 = Restaurant.create(
                "Restaurant 1",
                "Description 1",
                address,
                "+49123456789",
                "test1@restaurant.com",
                CuisineType.ITALIAN,
                true
        );

        Restaurant restaurant2 = Restaurant.create(
                "Restaurant 2",
                "Description 2",
                address,
                "+49987654321",
                "test2@restaurant.com",
                CuisineType.ASIAN,
                true
        );

        // Then
        assertThat(restaurant1.getId()).isNotEqualTo(restaurant2.getId());
    }

    @Test
    void create_ShouldHandleNullDescription() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");

        // When
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                null,
                address,
                "+49123456789",
                "test@restaurant.com",
                CuisineType.ITALIAN,
                true
        );

        // Then
        assertThat(restaurant.getDescription()).isNull();
    }

    @Test
    void create_ShouldCreateRestaurantWithAllCuisineTypes() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");

        // Test all cuisine types
        for (CuisineType cuisineType : CuisineType.values()) {
            // When
            Restaurant restaurant = Restaurant.create(
                    "Test Restaurant",
                    "Description",
                    address,
                    "+49123456789",
                    "test@restaurant.com",
                    cuisineType,
                    true
            );

            // Then
            assertThat(restaurant.getCuisineType()).isEqualTo(cuisineType);
        }
    }
}
