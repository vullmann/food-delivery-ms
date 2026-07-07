package de.ullmann.fooddelivery.common.messaging;

public interface MessagePublisher {
    void publish(String topic, String aggregateId, String payload);
}
