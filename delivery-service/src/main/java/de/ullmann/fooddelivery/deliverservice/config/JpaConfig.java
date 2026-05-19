package de.ullmann.fooddelivery.deliverservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "de.ullmann.fooddelivery.deliverservice.repository",
        "de.ullmann.fooddelivery.common.outbox"
})
public class JpaConfig {
    public JpaConfig() {
        // this constructor is executed when spring loads the context
    }
}
