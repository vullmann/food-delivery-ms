package de.ullmann.fooddelivery.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${clients.customer-service-url}")
    private String customerServiceUrl;

    @Bean
    public RestClient customerClient() {
        return RestClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }
}
