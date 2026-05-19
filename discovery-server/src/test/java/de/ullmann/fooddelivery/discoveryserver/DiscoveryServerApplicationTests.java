package de.ullmann.fooddelivery.discoveryserver;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class DiscoveryServerApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts successfully.
    }

    @Test
    void main_shouldStartApplicationWithoutErrors() {
        assertThatNoException().isThrownBy(() ->
                DiscoveryServerApplication.main(new String[]{})
        );
    }
}