package de.ullmann.fooddelivery.customerservice.config;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Tests whether JpaConfig correctly activates the repositories.
 * We use @DataJpaTest because it starts an in-memory database (H2)
 * and tests the JPA context in isolation.
 */
@DataJpaTest
@Import(JpaConfig.class) // Explicitly imports your new configuration
class JpaConfigTest {

    @Autowired
    private JpaConfig jpaConfig;

    @Autowired
    private ApplicationContext context;

    @Test
    void testCoverage() {
        assertThat(jpaConfig).isNotNull();
    }

    @Test
    void contextLoadsWithJpaConfig() {
        // Verifies that the context starts successfully with JpaConfig
        assertThat(context).isNotNull();
    }

    @Test
    void repositoriesArePresentInContext() {
        // Verifies that beans from the configured packages are present.
        // You can replace the generic check with a specific 'CustomerRepository.class' check if preferred.

        String[] beanNames = context.getBeanDefinitionNames();

        // Check if JPA repositories were loaded (Spring usually generates bean names containing 'repository')
        boolean hasRepositories = false;
        for (String name : beanNames) {
            if (name.toLowerCase().contains("repository")) {
                hasRepositories = true;
                break;
            }
        }

        assertThat(hasRepositories)
                .as("JPA repositories should be present in the application context")
                .isTrue();
    }
}