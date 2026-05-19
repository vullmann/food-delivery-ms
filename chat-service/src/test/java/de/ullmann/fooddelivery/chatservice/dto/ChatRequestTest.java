package de.ullmann.fooddelivery.chatservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ChatRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldBeValidWithSessionIdAndMessage() {
        ChatRequest req = new ChatRequest("session-1", "hello");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithNullSessionId() {
        ChatRequest req = new ChatRequest(null, "hello");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenMessageIsBlank() {
        ChatRequest req = new ChatRequest("s1", "");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("message"));
    }

    @Test
    void shouldFailWhenMessageIsNull() {
        ChatRequest req = new ChatRequest("s1", null);

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("message"));
    }

    @Test
    void shouldExposeAccessors() {
        ChatRequest req = new ChatRequest("my-session", "hi there");

        assertThat(req.sessionId()).isEqualTo("my-session");
        assertThat(req.message()).isEqualTo("hi there");
    }
}
