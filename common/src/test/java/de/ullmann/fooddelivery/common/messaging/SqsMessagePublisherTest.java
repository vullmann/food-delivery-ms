package de.ullmann.fooddelivery.common.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.awspring.cloud.sqs.operations.SqsSendOptions;
import io.awspring.cloud.sqs.operations.SqsTemplate;

@SuppressWarnings({"unchecked", "rawtypes"})
class SqsMessagePublisherTest {

    @Test
    void publish_shouldConvertDotsToHyphensAndSendToQueue() {
        SqsTemplate sqsTemplate = mock(SqsTemplate.class);
        SqsMessagePublisher publisher = new SqsMessagePublisher(sqsTemplate);
        String payload = "{\"orderId\":\"123\"}";

        publisher.publish("order.placed", "agg-id", payload);

        ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(sqsTemplate).send(captor.capture());

        SqsSendOptions options = mock(SqsSendOptions.class);
        when(options.queue(anyString())).thenReturn(options);
        when(options.payload(any())).thenReturn(options);
        captor.getValue().accept(options);

        verify(options).queue("order-placed");
        verify(options).payload(payload);
    }
}
