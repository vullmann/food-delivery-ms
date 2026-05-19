package de.ullmann.fooddelivery.customerservice;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest(classes = CustomerServiceApplication.class)
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.jpa.open-in-view=false"
})
class CustomerServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts successfully.
    }

    @Test
    void main_shouldStartApplicationWithoutErrors() {
        assertThatNoException().isThrownBy(() ->
                CustomerServiceApplication.main(new String[]{})
        );
    }
}
