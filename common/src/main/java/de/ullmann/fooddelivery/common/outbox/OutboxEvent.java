package de.ullmann.fooddelivery.common.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String aggregateType;        // e.g.: "CustomerOrder", "RestaurantOrder"

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;            // e.g.: "customer.created", "order.placed"

    @Column(nullable = false)
    private String payloadType;            // e.g.: "OrderPlacedEvent", needed for dezerialisierung

    @Column(columnDefinition = "TEXT")
    private String payload;              // JSON as String

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;   // null = not sent yet
}