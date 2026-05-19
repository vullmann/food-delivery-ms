package de.ullmann.fooddelivery.mcpservice.client;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import de.ullmann.fooddelivery.mcpservice.dto.OrderResponse;

@Component
public class OrderServiceClient {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceClient.class);

    private final RestClient restClient;

    public OrderServiceClient(@Qualifier("orderRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<OrderResponse> getOrderById(String orderId) {
        try {
            OrderResponse response = restClient.get()
                    .uri("/orders/{id}", orderId)
                    .retrieve()
                    .body(OrderResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException e) {
            log.warn("Order API error for ID {}: Status {}", orderId, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Network timeout reaching order service for ID {}", orderId, e);
            return Optional.empty();
        }
    }

    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        try {
            List<OrderResponse> response = restClient.get()
                    .uri("/orders/customer/{id}", customerId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return response != null ? response : List.of();
        } catch (RestClientResponseException e) {
            log.warn("Order API error for customer ID {}: Status {}", customerId, e.getStatusCode());
            return List.of();
        } catch (ResourceAccessException e) {
            log.error("Network timeout reaching order service for customer ID {}", customerId, e);
            return List.of();
        }
    }
}
