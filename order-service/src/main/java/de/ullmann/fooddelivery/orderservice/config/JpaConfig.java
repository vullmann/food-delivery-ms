package de.ullmann.fooddelivery.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "de.ullmann.fooddelivery.orderservice.repository",
        "de.ullmann.fooddelivery.common.outbox"
})
public class JpaConfig {
    public JpaConfig() {
        // this constructor is executed when spring loads the context
    }
}
