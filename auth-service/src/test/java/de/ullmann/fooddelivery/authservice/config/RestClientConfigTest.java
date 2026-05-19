package de.ullmann.fooddelivery.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {

    @Test
    void customerClient_shouldReturnRestClient() {
        RestClientConfig config = new RestClientConfig();
        ReflectionTestUtils.setField(config, "customerServiceUrl", "http://localhost:8080");

        RestClient client = config.customerClient();

        assertThat(client).isNotNull();
    }
}
