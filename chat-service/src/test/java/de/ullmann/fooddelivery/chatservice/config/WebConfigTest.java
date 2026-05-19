package de.ullmann.fooddelivery.chatservice.config;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class WebConfigTest {

    private final WebConfig webConfig = new WebConfig();

    @Test
    void addCorsMappings_shouldRegisterWithoutError() {
        CorsRegistry registry = new CorsRegistry();

        assertThatNoException().isThrownBy(() -> webConfig.addCorsMappings(registry));
    }
}
