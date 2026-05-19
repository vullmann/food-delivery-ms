package de.ullmann.fooddelivery.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemCategory category;

    @Column(nullable = false)
    private boolean available;

    public static MenuItem create(Restaurant restaurant, String name, String description,
                                  BigDecimal price, MenuItemCategory category, boolean available) {
        var item = new MenuItem();
        item.id = UUID.randomUUID();
        item.restaurant = restaurant;
        item.name = name;
        item.description = description;
        item.price = price;
        item.category = category;
        item.available = available;
        return item;
    }

    public void update(String name, String description, BigDecimal price,
                       MenuItemCategory category, boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = available;
    }
}