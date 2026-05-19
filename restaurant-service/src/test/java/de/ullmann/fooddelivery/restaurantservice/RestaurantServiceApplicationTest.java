package de.ullmann.fooddelivery.restaurantservice;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest(classes = RestaurantServiceApplication.class)
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.jpa.open-in-view=false"
})
class RestaurantServiceApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main_shouldStartApplicationWithoutErrors() {
        assertThatNoException().isThrownBy(() -> RestaurantServiceApplication.main(new String[]{}));
    }
}