package de.ullmann.fooddelivery.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCredential {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID customerId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserCredential create(UUID customerId, String email, String hashedPassword) {
        var credential = new UserCredential();
        credential.id = UUID.randomUUID();
        credential.customerId = customerId;
        credential.email = email;
        credential.hashedPassword = hashedPassword;
        return credential;
    }
}
