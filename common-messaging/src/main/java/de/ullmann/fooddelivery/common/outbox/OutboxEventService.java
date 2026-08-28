package de.ullmann.fooddelivery.common.outbox;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public void createEvent(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            Object payloadObject
    ) {
        String payload = toJson(payloadObject);
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payloadType(payloadObject.getClass().getSimpleName())
                .payload(payload)
                .build();
        event.setCreatedAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        outboxEventRepository.save(event);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Exception during JSON-serialization", e);
        }
    }
}