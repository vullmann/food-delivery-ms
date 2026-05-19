package de.ullmann.fooddelivery.deliverservice.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import de.ullmann.fooddelivery.common.model.Address;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryOrder {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID restaurantId;

    private UUID driverId;

    @Embedded
    @AttributeOverride(name = "street", column = @Column(name = "pickup_street"))
    @AttributeOverride(name = "houseNumber", column = @Column(name = "pickup_house_number"))
    @AttributeOverride(name = "city", column = @Column(name = "pickup_city"))
    @AttributeOverride(name = "zip", column = @Column(name = "pickup_zip"))
    @AttributeOverride(name = "country", column = @Column(name = "pickup_country"))
    private Address pickupAddress;

    @Embedded
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private DeliveryOrder(
            UUID orderId,
            UUID customerId,
            UUID restaurantId,
            Address pickupAddress,
            Address deliveryAddress) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.status = DeliveryStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public static DeliveryOrder create(
            UUID orderId,
            UUID customerId,
            UUID restaurantId,
            Address pickupAddress,
            Address deliveryAddress) {
        return new DeliveryOrder(orderId, customerId, restaurantId, pickupAddress, deliveryAddress);
    }

    public void assignDriver(UUID driverId) {
        transitionTo(DeliveryStatus.DRIVER_ASSIGNED);
        this.driverId = driverId;
    }

    public void transitionTo(DeliveryStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from %s to %s".formatted(this.status, newStatus));
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
