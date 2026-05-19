package de.ullmann.fooddelivery.orderservice.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.UUID;

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
    void handleNotFound_shouldReturnNotFoundProblemDetail() {
        UUID orderId = UUID.randomUUID();
        CustomerOrderNotFoundException exception = new CustomerOrderNotFoundException(orderId);

        ProblemDetail problemDetail = handler.handleNotFound(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Order not found: " + orderId, problemDetail.getDetail());
    }

    @Test
    void handleIllegalState_shouldReturnConflictProblemDetail() {
        IllegalStateException exception = new IllegalStateException("Cannot transition from PENDING to DELIVERED");

        ProblemDetail problemDetail = handler.handleIllegalState(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Cannot transition from PENDING to DELIVERED", problemDetail.getDetail());
    }

    @Test
    void handleValidation_shouldReturnBadRequestProblemDetail() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "field1", "must not be null"));
        bindingResult.addError(new FieldError("testObject", "field2", "must be positive"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail problemDetail = handler.handleValidation(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertNotNull(problemDetail.getDetail());
        assertTrue(problemDetail.getDetail().contains("field1"));
        assertTrue(problemDetail.getDetail().contains("must not be null"));
        assertTrue(problemDetail.getDetail().contains("field2"));
        assertTrue(problemDetail.getDetail().contains("must be positive"));
    }

    @Test
    void handleValidation_shouldHandleSingleFieldError() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "customerId", "must not be null"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail problemDetail = handler.handleValidation(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertTrue(problemDetail.getDetail().contains("customerId: must not be null"));
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
