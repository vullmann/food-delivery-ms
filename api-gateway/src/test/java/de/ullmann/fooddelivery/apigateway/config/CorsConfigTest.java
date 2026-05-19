package de.ullmann.fooddelivery.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void corsWebFilter_shouldReturnConfiguredFilter() {
        CorsWebFilter filter = corsConfig.corsWebFilter();

        assertThat(filter).isNotNull();
    }
}
