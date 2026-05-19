package de.ullmann.fooddelivery.notificationservice.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_phone_projections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerPhoneProjection {

    @Id
    private UUID customerId;

    @Column(nullable = false)
    private String phone;

    private CustomerPhoneProjection(UUID customerId, String phone) {
        this.customerId = customerId;
        this.phone = phone;
    }

    public static CustomerPhoneProjection of(UUID customerId, String phone) {
        return new CustomerPhoneProjection(customerId, phone);
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }
}
