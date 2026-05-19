package de.ullmann.fooddelivery.common.outbox;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> outboxKafkaTemplate;

    @InjectMocks
    private OutboxEventProcessor outboxEventProcessor;

    private OutboxEvent outboxEvent;

    @BeforeEach
    void setUp() {
        outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setAggregateId(UUID.randomUUID());
        outboxEvent.setEventType(OrderPlacedEvent.TOPIC);
        outboxEvent.setPayloadType(OrderPlacedEvent.class.getSimpleName());
        outboxEvent.setPayload("{\"orderId\": \"123\"}");
        outboxEvent.setProcessedAt(null);
    }

    @Test
    void processNextBatch_shouldPublishAndMarkEvents() {
        when(outboxEventRepository.findTopUnprocessedEvents(anyInt())).thenReturn(List.of(outboxEvent));

        outboxEventProcessor.processNextBatch(100);

        verify(outboxKafkaTemplate, times(1)).send(
                OrderPlacedEvent.TOPIC,
                outboxEvent.getAggregateId().toString(),
                outboxEvent.getPayload()
        );
        assertNotNull(outboxEvent.getProcessedAt());
    }

    @Test
    void processNextBatch_shouldNotPublishWhenNoEventsFounds() {
        when(outboxEventRepository.findTopUnprocessedEvents(anyInt())).thenReturn(List.of());

        outboxEventProcessor.processNextBatch(100);

        verify(outboxKafkaTemplate, never()).send(
                anyString(),
                anyString(),
                anyString()
        );
    }
}
