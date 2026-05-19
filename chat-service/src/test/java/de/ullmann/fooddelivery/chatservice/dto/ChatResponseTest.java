package de.ullmann.fooddelivery.chatservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatResponseTest {

    @Test
    void shouldExposeAccessors() {
        ChatResponse resp = new ChatResponse("session-abc", "Hello!");

        assertThat(resp.sessionId()).isEqualTo("session-abc");
        assertThat(resp.reply()).isEqualTo("Hello!");
    }

    @Test
    void shouldSupportNullFields() {
        ChatResponse resp = new ChatResponse(null, null);

        assertThat(resp.sessionId()).isNull();
        assertThat(resp.reply()).isNull();
    }
}
