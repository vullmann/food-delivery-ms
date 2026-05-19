package de.ullmann.fooddelivery.customerservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "de.ullmann.fooddelivery.customerservice.repository",
        "de.ullmann.fooddelivery.common.outbox"
})
public class JpaConfig {
    public JpaConfig() {
        // this constructor is executed when spring loads the context
    }
}
