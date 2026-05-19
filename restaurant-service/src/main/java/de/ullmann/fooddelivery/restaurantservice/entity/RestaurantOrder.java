package de.ullmann.fooddelivery.restaurantservice.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import de.ullmann.fooddelivery.common.model.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantOrder {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerOrderId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Column(nullable = false)
    private UUID customerId;

    @OneToMany(mappedBy = "restaurantOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<RestaurantOrderItem> items = new ArrayList<>();

    @Embedded
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantOrderStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private RestaurantOrder(
            UUID customerOrderId,
            UUID restaurantId,
            UUID customerId,
            Address deliveryAddress) {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.customerOrderId = customerOrderId;
        this.restaurantId = restaurantId;
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.status = RestaurantOrderStatus.RECEIVED;
    }

    public static RestaurantOrder create(
            UUID customerOrderId,
            UUID restaurantId,
            UUID customerId,
            Address deliveryAddress) {
        return new RestaurantOrder(customerOrderId, restaurantId, customerId, deliveryAddress);
    }

    public void addItem(RestaurantOrderItem item) {
        item.assignToRestaurantOrder(this);
        items.add(item);
    }

    public void transitionTo(RestaurantOrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from %s to %s".formatted(this.status, newStatus));
        }
        this.status = newStatus;
    }
}