package de.ullmann.fooddelivery.common.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SqsPayloadExtractor {

    private SqsPayloadExtractor() {}

    /**
     * Unwraps an SNS notification envelope if present, otherwise returns the body as-is.
     * SNS delivers messages to SQS wrapped in: {"Type":"Notification","Message":"<actual json>", ...}
     */
    public static String extract(String sqsBody, ObjectMapper objectMapper) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(sqsBody);
        if ("Notification".equals(root.path("Type").asText())) {
            return root.get("Message").asText();
        }
        return sqsBody;
    }
}
