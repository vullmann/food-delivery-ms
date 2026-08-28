package de.ullmann.fooddelivery.orderservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.ullmann.fooddelivery.common.model.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Embedded
    private Address deliveryAddress;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerOrderStatus status;

    @Enumerated(EnumType.STRING)
    private CancellationOrigin cancelledBy;

    private String cancellationReason;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_id")
    private final List<CustomerOrderItem> items = new ArrayList<>();

    private CustomerOrder(
            UUID customerId,
            UUID restaurantId,
            Address deliveryAddress,
            List<CustomerOrderItem> items) {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.status = CustomerOrderStatus.CREATED;

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        items.forEach(this::addItem);
        recalculateTotal();
    }

    public static CustomerOrder create(
            UUID customerId,
            UUID restaurantId,
            Address deliveryAddress,
            List<CustomerOrderItem> items) {
        return new CustomerOrder(customerId, restaurantId, deliveryAddress, items);
    }

    private void addItem(CustomerOrderItem item) {
        items.add(item);
        item.assignToCustomerOrder(this);
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(CustomerOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void transitionTo(CustomerOrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from %s to %s".formatted(this.status, newStatus));
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}