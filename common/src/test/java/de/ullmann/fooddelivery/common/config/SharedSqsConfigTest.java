package de.ullmann.fooddelivery.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

class SharedSqsConfigTest {

    @Test
    void sqsTemplate_shouldReturnNonNull() {
        SharedSqsConfig config = new SharedSqsConfig();
        SqsAsyncClient mockClient = mock(SqsAsyncClient.class);

        SqsTemplate template = config.sqsTemplate(mockClient);

        assertThat(template).isNotNull();
    }
}
