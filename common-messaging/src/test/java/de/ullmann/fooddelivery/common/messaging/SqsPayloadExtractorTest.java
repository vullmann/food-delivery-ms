package de.ullmann.fooddelivery.common.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class SqsPayloadExtractorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extract_shouldUnwrapSnsEnvelope() throws Exception {
        String innerMessage = "{\"orderId\":\"123\"}";
        String snsEnvelope = objectMapper.writeValueAsString(Map.of(
                "Type", "Notification",
                "Message", innerMessage
        ));

        String result = SqsPayloadExtractor.extract(snsEnvelope, objectMapper);

        assertThat(result).isEqualTo(innerMessage);
    }

    @Test
    void extract_shouldReturnBodyAsIsWhenNotSnsNotification() throws Exception {
        String directPayload = "{\"orderId\":\"123\"}";

        String result = SqsPayloadExtractor.extract(directPayload, objectMapper);

        assertThat(result).isEqualTo(directPayload);
    }

    @Test
    void constructor_shouldBePrivate() throws Exception {
        Constructor<SqsPayloadExtractor> constructor = SqsPayloadExtractor.class.getDeclaredConstructor();
        assertThat(constructor.canAccess(null)).isFalse();
        constructor.setAccessible(true);
        assertThat(constructor.newInstance()).isInstanceOf(SqsPayloadExtractor.class);
    }
}
