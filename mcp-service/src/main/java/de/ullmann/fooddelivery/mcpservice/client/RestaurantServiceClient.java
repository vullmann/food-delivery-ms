package de.ullmann.fooddelivery.mcpservice.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import de.ullmann.fooddelivery.mcpservice.dto.RestaurantResponse;

@Component
public class RestaurantServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceClient.class);

    private final RestClient restClient;

    public RestaurantServiceClient(@Qualifier("restaurantRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<RestaurantResponse> getRestaurantById(String restaurantId) {
        try {
            RestaurantResponse response = restClient.get()
                    .uri("/restaurants/{id}", restaurantId)
                    .retrieve()
                    .body(RestaurantResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException e) {
            log.warn("Restaurant API error for ID {}: Status {}", restaurantId, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Network timeout reaching restaurant service for ID {}", restaurantId, e);
            return Optional.empty();
        }
    }
}
