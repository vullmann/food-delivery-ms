package de.ullmann.fooddelivery.chatservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String sessionId,  // null/absent on first message → new session is created
        @NotBlank(message = "message must not be blank") String message
) {}
