package de.ullmann.fooddelivery.common.messaging;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaMessagePublisherTest {

    @Mock
    private KafkaTemplate<String, String> outboxKafkaTemplate;

    @InjectMocks
    private KafkaMessagePublisher kafkaMessagePublisher;

    @Test
    void publish_shouldDelegateToKafkaTemplate() {
        kafkaMessagePublisher.publish("order.placed", "agg-id-123", "{\"orderId\":\"123\"}");

        verify(outboxKafkaTemplate, times(1)).send("order.placed", "agg-id-123", "{\"orderId\":\"123\"}");
    }
}
