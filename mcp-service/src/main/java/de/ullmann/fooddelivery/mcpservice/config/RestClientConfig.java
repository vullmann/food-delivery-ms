package de.ullmann.fooddelivery.mcpservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${mcp.clients.connect-timeout-seconds:5}")
    private int connectTimeoutSeconds;

    @Value("${mcp.clients.read-timeout-seconds:10}")
    private int readTimeoutSeconds;

    @Bean
    public RestClient orderRestClient(@Value("${mcp.clients.order-service-url}") String baseUrl) {
        return restClient(baseUrl);
    }

    @Bean
    public RestClient customerRestClient(@Value("${mcp.clients.customer-service-url}") String baseUrl) {
        return restClient(baseUrl);
    }

    @Bean
    public RestClient deliveryRestClient(@Value("${mcp.clients.delivery-service-url}") String baseUrl) {
        return restClient(baseUrl);
    }

    @Bean
    public RestClient restaurantRestClient(@Value("${mcp.clients.restaurant-service-url}") String baseUrl) {
        return restClient(baseUrl);
    }

    private RestClient restClient(String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
