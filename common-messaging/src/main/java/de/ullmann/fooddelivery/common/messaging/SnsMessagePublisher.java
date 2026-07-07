package de.ullmann.fooddelivery.common.messaging;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.awspring.cloud.sns.core.SnsNotification;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class SnsMessagePublisher implements MessagePublisher {

    private final SnsTemplate snsTemplate;

    @Override
    public void publish(String topic, String aggregateId, String payload) {
        String snsTopicName = topic.replace(".", "-");
        snsTemplate.sendNotification(snsTopicName, SnsNotification.of(payload));
    }
}
