package de.ullmann.fooddelivery.restaurantservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.assertj.core.api.Assertions.assertThatNoException;

class WebConfigTest {

    private final WebConfig webConfig = new WebConfig();

    @Test
    void addCorsMappings_shouldRegisterWithoutError() {
        CorsRegistry registry = new CorsRegistry();
        assertThatNoException().isThrownBy(() -> webConfig.addCorsMappings(registry));
    }
}
