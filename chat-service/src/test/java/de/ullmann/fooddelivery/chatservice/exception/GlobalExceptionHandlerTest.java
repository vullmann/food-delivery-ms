package de.ullmann.fooddelivery.chatservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleUnauthorized_shouldReturn401WithMessage() {
        UnauthorizedException ex = new UnauthorizedException("not allowed");

        ProblemDetail result = handler.handleUnauthorized(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getDetail()).isEqualTo("not allowed");
    }

    @Test
    void handleValidation_shouldReturn400WithFieldErrors() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "req");
        binding.addError(new FieldError("req", "message", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);

        ProblemDetail result = handler.handleValidation(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Validation failed");
        assertThat(result.getDetail()).contains("message: must not be blank");
    }

    @Test
    void handleValidation_shouldReturn400WithFallbackWhenNoErrors() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "req");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);

        ProblemDetail result = handler.handleValidation(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).isEqualTo("Invalid request");
    }

    @Test
    void handleValidation_shouldConcatenateMultipleFieldErrors() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "req");
        binding.addError(new FieldError("req", "message", "must not be blank"));
        binding.addError(new FieldError("req", "sessionId", "too long"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);

        ProblemDetail result = handler.handleValidation(ex);

        assertThat(result.getDetail()).contains("message: must not be blank");
        assertThat(result.getDetail()).contains("sessionId: too long");
    }

    @Test
    void handleGeneric_shouldReturn500WithMessage() {
        Exception ex = new RuntimeException("something broke");

        ProblemDetail result = handler.handleGeneric(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Chat error");
        assertThat(result.getDetail()).isEqualTo("something broke");
    }
}
