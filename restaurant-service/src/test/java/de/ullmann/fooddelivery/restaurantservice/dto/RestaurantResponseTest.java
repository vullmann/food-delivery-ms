package de.ullmann.fooddelivery.restaurantservice.dto;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.restaurantservice.entity.CuisineType;
import de.ullmann.fooddelivery.restaurantservice.entity.Restaurant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantResponseTest {

    @Test
    void from_ShouldMapAllFieldsFromRestaurant() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "A great place to eat",
                address,
                "+49123456789",
                "test@restaurant.com",
                CuisineType.ITALIAN,
                true
        );

        // When
        RestaurantResponse response = RestaurantResponse.from(restaurant);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(restaurant.getId());
        assertThat(response.name()).isEqualTo("Test Restaurant");
        assertThat(response.description()).isEqualTo("A great place to eat");
        assertThat(response.address()).isEqualTo(address);
        assertThat(response.phone()).isEqualTo("+49123456789");
        assertThat(response.email()).isEqualTo("test@restaurant.com");
        assertThat(response.cuisineType()).isEqualTo(CuisineType.ITALIAN);
        assertThat(response.isOpen()).isTrue();
        assertThat(response.createdAt()).isEqualTo(restaurant.getCreatedAt());
    }

    @Test
    void from_ShouldHandleClosedRestaurant() {
        // Given
        Address address = Address.of("Side Street", "456", "Munich", "80331", "Germany");
        Restaurant restaurant = Restaurant.create(
                "Closed Restaurant",
                "Currently closed",
                address,
                "+49987654321",
                "closed@restaurant.com",
                CuisineType.ASIAN,
                false
        );

        // When
        RestaurantResponse response = RestaurantResponse.from(restaurant);

        // Then
        assertThat(response.isOpen()).isFalse();
    }

    @Test
    void from_ShouldHandleNullDescription() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                null,
                address,
                "+49123456789",
                "test@restaurant.com",
                CuisineType.PIZZA,
                true
        );

        // When
        RestaurantResponse response = RestaurantResponse.from(restaurant);

        // Then
        assertThat(response.description()).isNull();
    }

    @Test
    void from_ShouldMapAllCuisineTypes() {
        // Given
        Address address = Address.of("Main Street", "123", "Berlin", "10115", "Germany");

        // Test all cuisine types
        for (CuisineType cuisineType : CuisineType.values()) {
            Restaurant restaurant = Restaurant.create(
                    "Test Restaurant",
                    "Description",
                    address,
                    "+49123456789",
                    "test@restaurant.com",
                    cuisineType,
                    true
            );

            // When
            RestaurantResponse response = RestaurantResponse.from(restaurant);

            // Then
            assertThat(response.cuisineType()).isEqualTo(cuisineType);
        }
    }

    @Test
    void from_ShouldPreserveAddressDetails() {
        // Given
        Address address = Address.of("Hauptstraße", "42A", "Frankfurt", "60311", "Deutschland");
        Restaurant restaurant = Restaurant.create(
                "German Restaurant",
                "Authentic German cuisine",
                address,
                "+49691234567",
                "german@restaurant.de",
                CuisineType.OTHER,
                true
        );

        // When
        RestaurantResponse response = RestaurantResponse.from(restaurant);

        // Then
        assertThat(response.address().getStreet()).isEqualTo("Hauptstraße");
        assertThat(response.address().getHouseNumber()).isEqualTo("42A");
        assertThat(response.address().getCity()).isEqualTo("Frankfurt");
        assertThat(response.address().getZip()).isEqualTo("60311");
        assertThat(response.address().getCountry()).isEqualTo("Deutschland");
    }

    @Test
    void from_ShouldCreateImmutableResponse() {
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

        // When
        RestaurantResponse response1 = RestaurantResponse.from(restaurant);
        RestaurantResponse response2 = RestaurantResponse.from(restaurant);

        // Then - Records are immutable, the same values should be equal
        assertThat(response1.id()).isEqualTo(response2.id());
        assertThat(response1.name()).isEqualTo(response2.name());
        assertThat(response1.email()).isEqualTo(response2.email());
    }
}
