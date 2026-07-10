package de.ullmann.fooddelivery.deliverservice.exception;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDeliveryOrderNotFound_shouldReturn404() {
        UUID id = UUID.randomUUID();
        ProblemDetail pd = handler.handleDeliveryOrderNotFound(new DeliveryOrderNotFoundException(id));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getTitle()).isEqualTo("Delivery Order Not Found");
    }

    @Test
    void handleDriverNotFound_shouldReturn404() {
        UUID id = UUID.randomUUID();
        ProblemDetail pd = handler.handleDriverNotFound(new DriverNotFoundException(id));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getTitle()).isEqualTo("Driver Not Found");
    }

    @Test
    void handleInsufficientRole_shouldReturn403() {
        ProblemDetail pd = handler.handleInsufficientRole(new InsufficientRoleException("not allowed"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(pd.getTitle()).isEqualTo("Insufficient Role");
        assertThat(pd.getDetail()).isEqualTo("not allowed");
    }

    @Test
    void handleDeliveryOrderAccessDenied_shouldReturn403() {
        UUID id = UUID.randomUUID();
        ProblemDetail pd = handler.handleDeliveryOrderAccessDenied(new DeliveryOrderAccessDeniedException(id));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(pd.getTitle()).isEqualTo("Delivery Order Access Denied");
        assertThat(pd.getDetail()).contains(id.toString());
    }

    @Test
    void handleValidation_shouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("obj", "firstName", "must not be blank")));

        ProblemDetail pd = handler.handleValidation(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getTitle()).isEqualTo("Validation Failed");
        assertThat(pd.getDetail()).contains("firstName");
    }

    @Test
    void handleTypeMismatch_shouldReturn400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("bad-value");
        when(ex.getName()).thenReturn("id");

        ProblemDetail pd = handler.handleTypeMismatch(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getTitle()).isEqualTo("Invalid Parameter");
        assertThat(pd.getDetail()).contains("bad-value");
    }

    @Test
    void handleIllegalArgument_shouldReturn409() {
        ProblemDetail pd = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getTitle()).isEqualTo("Invalid Request");
    }

    @Test
    void handleIllegalState_shouldReturn409() {
        ProblemDetail pd = handler.handleIllegalState(new IllegalStateException("bad state"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getTitle()).isEqualTo("Invalid Status Transition");
    }
}
