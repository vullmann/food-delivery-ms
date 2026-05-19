package de.ullmann.fooddelivery.customerservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_shouldReturnNotFoundProblemDetailForUUID() {
        UUID customerId = UUID.randomUUID();
        CustomerNotFoundException exception = new CustomerNotFoundException(customerId);

        ProblemDetail problemDetail = handler.handleNotFound(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Customer not found: " + customerId, problemDetail.getDetail());
    }

    @Test
    void handleNotFound_shouldReturnNotFoundProblemDetailForEmail() {
        String email = "test@example.com";
        CustomerNotFoundException exception = new CustomerNotFoundException(email);

        ProblemDetail problemDetail = handler.handleNotFound(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Customer not found: " + email, problemDetail.getDetail());
    }

    @Test
    void handleEmailAlreadyInUse_shouldReturnConflictProblemDetail() {
        EmailAlreadyInUseException exception = new EmailAlreadyInUseException("test@example.com");

        ProblemDetail problemDetail = handler.handleEmailAlreadyInUse(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Email already in use: test@example.com", problemDetail.getDetail());
    }

    @Test
    void handleIllegalState_shouldReturnConflictProblemDetail() {
        IllegalStateException exception = new IllegalStateException("Invalid state transition");

        ProblemDetail problemDetail = handler.handleIllegalState(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Invalid state transition", problemDetail.getDetail());
    }

    @Test
    void handleValidation_shouldReturnBadRequestProblemDetail() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "firstName", "must not be blank"));
        bindingResult.addError(new FieldError("testObject", "email", "must be a valid email"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail problemDetail = handler.handleValidation(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertNotNull(problemDetail.getDetail());
        assertTrue(problemDetail.getDetail().contains("firstName"));
        assertTrue(problemDetail.getDetail().contains("must not be blank"));
        assertTrue(problemDetail.getDetail().contains("email"));
        assertTrue(problemDetail.getDetail().contains("must be a valid email"));
    }

    @Test
    void handleValidation_shouldHandleSingleFieldError() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "email", "must not be blank"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail problemDetail = handler.handleValidation(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertTrue(problemDetail.getDetail().contains("email: must not be blank"));
    }

    @Test
    void handleValidation_shouldHandleEmptyBindingResult() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail problemDetail = handler.handleValidation(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertNotNull(problemDetail.getDetail());
    }
}
