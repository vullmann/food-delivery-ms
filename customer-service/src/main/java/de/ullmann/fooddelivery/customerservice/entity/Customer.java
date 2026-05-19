package de.ullmann.fooddelivery.customerservice.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import de.ullmann.fooddelivery.common.model.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Embedded
    private Address address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Customer create(
            String firstName,
            String lastName,
            String email,
            String password,
            String phone,
            Address address) {
        var customer = new Customer();
        customer.id = UUID.randomUUID();
        customer.createdAt = LocalDateTime.now();
        customer.firstName = firstName;
        customer.lastName = lastName;
        customer.email = email;
        customer.password = password;
        customer.phone = phone;
        customer.address = address;
        return customer;
    }

    public void update(
            String firstName,
            String lastName,
            String phone,
            Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
    }
}