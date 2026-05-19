package de.ullmann.fooddelivery.common.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;


    @Transactional // This now works because it's called from an outside bean
    public int processNextBatch(int batchSize) {
        // 1. Fetch and Lock
        List<OutboxEvent> events = repository.findTopUnprocessedEvents(batchSize);

        if (events.isEmpty()) {
            return 0;
        }

        for (OutboxEvent event : events) {
            try {
                outboxKafkaTemplate.send(event.getEventType(), event.getAggregateId().toString(), event.getPayload());

                // 3. Update Status
                event.setProcessedAt(LocalDateTime.now());
            } catch (Exception e) {
                // log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                // event.setStatus(OutboxStatus.FAILED); // Implement retry logic later
            }
        }
        return events.size();
    }
}
