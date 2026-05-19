package de.ullmann.fooddelivery.common.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventProcessor outboxEventProcessor;

    @Scheduled(
            fixedDelayString = "${app.outbox.interval-ms:5000}",
            initialDelay = 10000 // Wait 10s for the app to fully boot in Docker
    )
    public void publish() {
        // Trigger the logic in another bean to ensure @Transactional works
        int processedCount = outboxEventProcessor.processNextBatch(100);
        if (processedCount > 0) {
            log.debug("Successfully processed {} outbox events", processedCount);
        } else {
            log.debug("No outbox events to process");
        }
    }
}