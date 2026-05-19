package de.ullmann.fooddelivery.orderservice.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
public class CustomerOrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    @Column(nullable = false)
    private UUID menuItemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private CustomerOrderItem(
            UUID menuItemId,
            String name,
            String description,
            Integer quantity,
            BigDecimal unitPrice) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public static CustomerOrderItem create(
            UUID menuItemId,
            String name,
            String description,
            Integer quantity,
            BigDecimal unitPrice) {
        return new CustomerOrderItem(menuItemId, name, description, quantity, unitPrice);
    }

    void assignToCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }
}