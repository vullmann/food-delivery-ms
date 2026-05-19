package de.ullmann.fooddelivery.mcpservice.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import de.ullmann.fooddelivery.mcpservice.dto.CustomerResponse;

@Component
public class CustomerServiceClient {

    private final RestClient restClient;

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceClient.class);

    public CustomerServiceClient(@Qualifier("customerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<CustomerResponse> getCustomerById(String customerId) {
        try {
            CustomerResponse response = restClient.get()
                    .uri("/customers/{id}", customerId)
                    .retrieve()
                    .body(CustomerResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException e) {
            // Catches 4xx and 5xx API responses (e.g. 404 Not Found) cleanly
            log.warn("Customer API error for ID {}: Status {}", customerId, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            // Catches network dropouts, I/O errors, and connection timeouts
            log.error("Network timeout reaching customer service for ID {}", customerId, e);
            return Optional.empty();
        }
    }

    public Optional<CustomerResponse> getCustomerByEmail(String email) {
        try {
            CustomerResponse response = restClient.get()
                    .uri("/customers?email={email}", email)
                    .retrieve()
                    .body(CustomerResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException e) {
            log.warn("Customer API error for email {}: Status {}", email, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Network timeout reaching customer service for email {}", email, e);
            return Optional.empty();
        }
    }
}
