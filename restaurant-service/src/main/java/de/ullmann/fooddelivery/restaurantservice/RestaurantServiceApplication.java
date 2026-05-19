package de.ullmann.fooddelivery.restaurantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "de.ullmann.fooddelivery")
@EnableScheduling
@EntityScan(basePackages = {
        "de.ullmann.fooddelivery.restaurantservice",
        "de.ullmann.fooddelivery.common.outbox"
})
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}