package de.ullmann.fooddelivery.mcpservice.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import de.ullmann.fooddelivery.mcpservice.dto.DeliveryResponse;

@Component
public class DeliveryServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceClient.class);

    private final RestClient restClient;

    public DeliveryServiceClient(@Qualifier("deliveryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<DeliveryResponse> getDeliveryByOrderId(String orderId) {
        try {
            DeliveryResponse response = restClient.get()
                    .uri("/deliveries?orderId={id}", orderId)
                    .retrieve()
                    .body(DeliveryResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException e) {
            log.warn("Delivery API error for order ID {}: Status {}", orderId, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Network timeout reaching delivery service for order ID {}", orderId, e);
            return Optional.empty();
        }
    }
}
