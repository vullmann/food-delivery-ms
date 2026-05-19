package de.ullmann.fooddelivery.orderservice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JpaConfigTest {

    @Test
    void jpaConfig_shouldInstantiateSuccessfully() {
        JpaConfig config = new JpaConfig();
        assertThat(config).isNotNull();
    }
}
