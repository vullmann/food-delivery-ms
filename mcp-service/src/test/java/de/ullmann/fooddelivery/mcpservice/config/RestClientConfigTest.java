package de.ullmann.fooddelivery.mcpservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {

    private RestClientConfig config;

    @BeforeEach
    void setUp() {
        config = new RestClientConfig();
        ReflectionTestUtils.setField(config, "connectTimeoutSeconds", 5);
        ReflectionTestUtils.setField(config, "readTimeoutSeconds", 10);
    }

    @Test
    void orderRestClient_shouldReturnRestClient() {
        RestClient client = config.orderRestClient("http://localhost:8082");
        assertThat(client).isNotNull();
    }

    @Test
    void customerRestClient_shouldReturnRestClient() {
        RestClient client = config.customerRestClient("http://localhost:8081");
        assertThat(client).isNotNull();
    }

    @Test
    void deliveryRestClient_shouldReturnRestClient() {
        RestClient client = config.deliveryRestClient("http://localhost:8084");
        assertThat(client).isNotNull();
    }

    @Test
    void restaurantRestClient_shouldReturnRestClient() {
        RestClient client = config.restaurantRestClient("http://localhost:8083");
        assertThat(client).isNotNull();
    }
}
