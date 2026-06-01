package de.ullmann.fooddelivery.common.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!aws")
public class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    public KafkaMessagePublisher(@Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> outboxKafkaTemplate) {
        this.outboxKafkaTemplate = outboxKafkaTemplate;
    }

    @Override
    public void publish(String topic, String aggregateId, String payload) {
        outboxKafkaTemplate.send(topic, aggregateId, payload);
    }
}
