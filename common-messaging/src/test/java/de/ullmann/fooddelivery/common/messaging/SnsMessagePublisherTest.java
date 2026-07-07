package de.ullmann.fooddelivery.common.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.awspring.cloud.sns.core.SnsNotification;
import io.awspring.cloud.sns.core.SnsTemplate;

@ExtendWith(MockitoExtension.class)
class SnsMessagePublisherTest {

    @Mock
    private SnsTemplate snsTemplate;

    @InjectMocks
    private SnsMessagePublisher snsMessagePublisher;

    @Test
    void publish_shouldConvertDotsToHyphensAndSendSnsNotification() {
        snsMessagePublisher.publish("order.placed", "agg-id", "{\"orderId\":\"123\"}");

        verify(snsTemplate).sendNotification(eq("order-placed"), any(SnsNotification.class));
    }
}
