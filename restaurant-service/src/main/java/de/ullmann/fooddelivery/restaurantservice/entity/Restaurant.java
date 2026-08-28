package de.ullmann.fooddelivery.restaurantservice.entity;

import de.ullmann.fooddelivery.common.model.Address;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Restaurant {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CuisineType cuisineType;

    @Column(nullable = false)
    private boolean isOpen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Restaurant create(String name,
                                    String description,
                                    Address address,
                                    String phone,
                                    String email,
                                    CuisineType cuisineType,
                                    boolean isOpen) {
        Restaurant restaurant = new Restaurant();
        restaurant.id = UUID.randomUUID();
        restaurant.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        restaurant.name = name;
        restaurant.description = description;
        restaurant.address = address;
        restaurant.phone = phone;
        restaurant.email = email;
        restaurant.cuisineType = cuisineType;
        restaurant.isOpen = isOpen;
        return restaurant;
    }

    public void update(String name,
                       String description,
                       Address address,
                       String phone,
                       String email,
                       CuisineType cuisineType,
                       boolean isOpen) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.cuisineType = cuisineType;
        this.isOpen = isOpen;
    }
}