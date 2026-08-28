package de.ullmann.fooddelivery.deliverservice.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drivers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Driver {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Driver(UUID id, String firstName, String lastName, String phone) {
        this.id = id;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.status = DriverStatus.AVAILABLE;
    }

    public static Driver create(String firstName, String lastName, String phone) {
        return new Driver(UUID.randomUUID(), firstName, lastName, phone);
    }

    public static Driver createWithId(UUID id, String firstName, String lastName, String phone) {
        return new Driver(id, firstName, lastName, phone);
    }

    public void markBusy() {
        this.status = DriverStatus.BUSY;
    }

    public void markAvailable() {
        this.status = DriverStatus.AVAILABLE;
    }

    public void markOffline() {
        this.status = DriverStatus.OFFLINE;
    }
}
