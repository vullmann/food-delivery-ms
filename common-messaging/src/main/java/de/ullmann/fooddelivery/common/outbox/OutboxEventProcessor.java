package de.ullmann.fooddelivery.common.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import de.ullmann.fooddelivery.common.messaging.MessagePublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventRepository repository;
    private final MessagePublisher messagePublisher;

    @Transactional
    public int processNextBatch(int batchSize) {
        List<OutboxEvent> events = repository.findTopUnprocessedEvents(batchSize);

        if (events.isEmpty()) {
            return 0;
        }

        for (OutboxEvent event : events) {
            try {
                messagePublisher.publish(event.getEventType(), event.getAggregateId().toString(), event.getPayload());
                event.setProcessedAt(LocalDateTime.now());
            } catch (Exception e) {
                // log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
        return events.size();
    }
}
