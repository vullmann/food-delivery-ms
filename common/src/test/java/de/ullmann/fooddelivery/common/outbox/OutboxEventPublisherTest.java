package de.ullmann.fooddelivery.common.outbox;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventProcessor outboxEventProcessor;

    @InjectMocks
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    void publish_whenEventsProcessed_shouldCallProcessNextBatch() {
        when(outboxEventProcessor.processNextBatch(100)).thenReturn(5);

        outboxEventPublisher.publish();

        verify(outboxEventProcessor).processNextBatch(100);
    }

    @Test
    void publish_whenNoEventsProcessed_shouldCallProcessNextBatch() {
        when(outboxEventProcessor.processNextBatch(100)).thenReturn(0);

        outboxEventPublisher.publish();

        verify(outboxEventProcessor).processNextBatch(100);
    }
}
