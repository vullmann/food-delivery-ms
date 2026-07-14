package de.ullmann.fooddelivery.authservice.exception;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailAlreadyRegistered_shouldReturn409() {
        ProblemDetail pd = handler.handleEmailAlreadyRegistered(new EmailAlreadyRegisteredException("user@example.com"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void handleInvalidCredentials_shouldReturn401() {
        ProblemDetail pd = handler.handleInvalidCredentials(new InvalidCredentialsException());
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void handleInvalidToken_shouldReturn401() {
        ProblemDetail pd = handler.handleInvalidToken(new InvalidTokenException());
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void handleInsufficientRole_shouldReturn403() {
        ProblemDetail pd = handler.handleInsufficientRole(new InsufficientRoleException("not allowed"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void handleValidation_shouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("obj", "email", "must be a valid email")));

        ProblemDetail pd = handler.handleValidation(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getDetail()).contains("email");
    }
}
