package de.ullmann.fooddelivery.restaurantservice.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantOrderItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_order_id", nullable = false)
    @JsonIgnore
    private RestaurantOrder restaurantOrder;

    @Column(nullable = false)
    private UUID menuItemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private RestaurantOrderItem(UUID menuItemId, String name, Integer quantity, BigDecimal unitPrice) {
        this.id = UUID.randomUUID();
        this.menuItemId = menuItemId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public static RestaurantOrderItem create(UUID menuItemId, String name, Integer quantity, BigDecimal unitPrice) {
        return new RestaurantOrderItem(menuItemId, name, quantity, unitPrice);
    }

    void assignToRestaurantOrder(RestaurantOrder restaurantOrder) {
        this.restaurantOrder = restaurantOrder;
    }
}
