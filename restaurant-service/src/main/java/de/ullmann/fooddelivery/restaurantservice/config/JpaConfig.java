package de.ullmann.fooddelivery.restaurantservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "de.ullmann.fooddelivery.restaurantservice.repository",
        "de.ullmann.fooddelivery.common.outbox"
})
public class JpaConfig {
    public JpaConfig() {
        // this constructor is executed when spring loads the context
    }
}
