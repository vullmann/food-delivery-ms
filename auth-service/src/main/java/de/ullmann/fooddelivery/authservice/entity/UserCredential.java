package de.ullmann.fooddelivery.authservice.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import de.ullmann.fooddelivery.common.security.Role;

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
@Table(name = "user_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCredential {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserCredential createCustomer(
            UUID userId,
            String email,
            String hashedPassword,
            String firstName,
            String lastName,
            String phone) {
        var credential = new UserCredential();
        credential.id = UUID.randomUUID();
        credential.userId = userId;
        credential.email = email;
        credential.hashedPassword = hashedPassword;
        credential.firstName = firstName;
        credential.lastName = lastName;
        credential.phone = phone;
        credential.role = Role.CUSTOMER;
        return credential;
    }

    public static UserCredential create(
            String email,
            String hashedPassword,
            String firstName,
            String lastName,
            String phone,
            Role role) {
        var credential = new UserCredential();
        credential.id = UUID.randomUUID();
        credential.userId = credential.id;
        credential.email = email;
        credential.hashedPassword = hashedPassword;
        credential.firstName = firstName;
        credential.lastName = lastName;
        credential.phone = phone;
        credential.role = role;
        return credential;
    }
}
