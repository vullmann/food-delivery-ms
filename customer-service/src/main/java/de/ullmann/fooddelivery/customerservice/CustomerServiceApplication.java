package de.ullmann.fooddelivery.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "de.ullmann.fooddelivery")
@EnableScheduling
@EntityScan(basePackages = {
        "de.ullmann.fooddelivery.customerservice",
        "de.ullmann.fooddelivery.common.outbox"
})
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}