package de.ullmann.fooddelivery.common.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxEventService outboxEventService;

    private UUID testAggregateId;
    private Map<String, String> testPayloadObject;

    private final String AGGREGATE_TYPE_CUSTOMER_ORDER = "CustomerOrder";

    @BeforeEach
    void setUp() {
        testAggregateId = UUID.randomUUID();
        testPayloadObject = Map.of(
                "orderId", "123",
                "customerId", "456"
        );
    }

    @Test
    void createEvent_shouldSaveOutboxEvent() throws Exception {
        // Arrange
        String expectedPayload = "{\"orderId\":\"123\",\"customerId\":\"456\"}";
        when(objectMapper.writeValueAsString(testPayloadObject)).thenReturn(expectedPayload);

        // Act
        outboxEventService.createEvent(
                AGGREGATE_TYPE_CUSTOMER_ORDER,
                testAggregateId,
                OrderPlacedEvent.TOPIC,
                testPayloadObject
        );

        // Assert: Überprüfe, ob OutboxEvent korrekt erstellt und gespeichert wurde
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(eventCaptor.capture());

        OutboxEvent savedEvent = eventCaptor.getValue();
        assertEquals(AGGREGATE_TYPE_CUSTOMER_ORDER, savedEvent.getAggregateType());
        assertEquals(testAggregateId, savedEvent.getAggregateId());
        assertEquals(OrderPlacedEvent.TOPIC, savedEvent.getEventType());
        assertEquals(expectedPayload, savedEvent.getPayload());
    }

    @Test
    void createEvent_shouldThrowExceptionOnSerializationError() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(testPayloadObject))
                .thenThrow(new RuntimeException("Serialization error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                outboxEventService.createEvent(
                        AGGREGATE_TYPE_CUSTOMER_ORDER,
                        testAggregateId,
                        OrderPlacedEvent.TOPIC,
                        testPayloadObject
                )
        );

        // Verify: Kein Event wird gespeichert, wenn die Serialisierung fehlschlägt
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    /*
    @Test
    void createEvent_shouldHandleNullPayload() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(null))
                .thenReturn("null");

        // Act & Assert
        assertDoesNotThrow(() ->
                outboxEventService.createEvent(
                        AGGREGATE_TYPE_CUSTOMER_ORDER,
                        testAggregateId,
                        OrderPlacedEvent.TOPIC,
                        null
                )
        );

        // Verify: Event wird trotzdem gespeichert (mit "null" als Payload)
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(eventCaptor.capture());
        assertEquals("null", eventCaptor.getValue().getPayload());
    }

     */
}