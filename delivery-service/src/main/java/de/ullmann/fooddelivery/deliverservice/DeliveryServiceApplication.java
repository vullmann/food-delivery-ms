package de.ullmann.fooddelivery.deliverservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "de.ullmann.fooddelivery")
@EnableScheduling
@EntityScan(basePackages = {
        "de.ullmann.fooddelivery.deliverservice",
        "de.ullmann.fooddelivery.common.outbox"
})
public class DeliveryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
    }
}