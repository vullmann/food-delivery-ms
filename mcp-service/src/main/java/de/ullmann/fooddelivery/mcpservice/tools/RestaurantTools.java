package de.ullmann.fooddelivery.mcpservice.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import de.ullmann.fooddelivery.mcpservice.client.RestaurantServiceClient;

@Service
public class RestaurantTools {

    private final RestaurantServiceClient restaurantServiceClient;

    public RestaurantTools(RestaurantServiceClient restaurantServiceClient) {
        this.restaurantServiceClient = restaurantServiceClient;
    }

    @Tool(description = "Get details about a restaurantId, including name, cuisine type, and whether it is currently open.")
    public String getRestaurant(String restaurantId) {
        return restaurantServiceClient.getRestaurantById(restaurantId)
                .map(r -> "Restaurant ID: " + r.id()
                        + "\nName: " + r.name()
                        + "\nDescription: " + r.description()
                        + "\nCuisine: " + r.cuisineType()
                        + "\nCurrently open: " + r.isOpen()
                        + (r.address() != null
                                ? "\nAddress: " + r.address().street() + " " + r.address().houseNumber()
                                  + ", " + r.address().zip() + " " + r.address().city()
                                  + ", " + r.address().country()
                                : ""))
                .orElse("No restaurant found with ID: " + restaurantId);
    }
}
