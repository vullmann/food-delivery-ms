package de.ullmann.fooddelivery.common.messaging;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;

// Superseded by SnsMessagePublisher (SNS fan-out for multi-consumer topics)
@Profile("aws")
@RequiredArgsConstructor
public class SqsMessagePublisher implements MessagePublisher {

    private final SqsTemplate sqsTemplate;

    @Override
    public void publish(String topic, String aggregateId, String payload) {
        // Kafka topic names use dots (order.placed); SQS queue names use hyphens (order-placed)
        String queueName = topic.replace(".", "-");
        sqsTemplate.send(to -> to.queue(queueName).payload(payload));
    }
}
