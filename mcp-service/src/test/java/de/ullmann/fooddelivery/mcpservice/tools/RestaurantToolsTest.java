package de.ullmann.fooddelivery.mcpservice.tools;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.mcpservice.client.RestaurantServiceClient;
import de.ullmann.fooddelivery.mcpservice.dto.RestaurantResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantToolsTest {

    @Mock
    private RestaurantServiceClient restaurantServiceClient;

    @InjectMocks
    private RestaurantTools restaurantTools;

    private static final String RESTAURANT_ID = "rest-id-123";

    @Test
    void getRestaurant_whenFoundWithAddress_shouldReturnFormattedInfo() {
        RestaurantResponse.Address address = new RestaurantResponse.Address("Via Roma", "1", "Rome", "00100", "Italy");
        RestaurantResponse response = new RestaurantResponse(RESTAURANT_ID, "Trattoria", "Italian", "ITALIAN", true, address);
        when(restaurantServiceClient.getRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(response));

        String result = restaurantTools.getRestaurant(RESTAURANT_ID);

        assertThat(result).contains("Trattoria").contains("ITALIAN").contains("Rome");
    }

    @Test
    void getRestaurant_whenFoundWithoutAddress_shouldReturnFormattedInfoWithoutAddress() {
        RestaurantResponse response = new RestaurantResponse(RESTAURANT_ID, "Trattoria", "Italian", "ITALIAN", true, null);
        when(restaurantServiceClient.getRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(response));

        String result = restaurantTools.getRestaurant(RESTAURANT_ID);

        assertThat(result).contains("Trattoria").doesNotContain("Address:");
    }

    @Test
    void getRestaurant_whenNotFound_shouldReturnNotFoundMessage() {
        when(restaurantServiceClient.getRestaurantById(RESTAURANT_ID)).thenReturn(Optional.empty());

        String result = restaurantTools.getRestaurant(RESTAURANT_ID);

        assertThat(result).contains("No restaurant found").contains(RESTAURANT_ID);
    }
}
