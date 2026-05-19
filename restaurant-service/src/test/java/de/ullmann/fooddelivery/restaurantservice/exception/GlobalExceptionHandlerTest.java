package de.ullmann.fooddelivery.restaurantservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleRestaurantNotFound_ShouldReturnNotFoundProblemDetail() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        RestaurantNotFoundException exception = new RestaurantNotFoundException(restaurantId);

        // When
        ProblemDetail problemDetail = exceptionHandler.handleRestaurantNotFound(exception);

        // Then
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Restaurant Not Found");
        assertThat(problemDetail.getDetail()).isEqualTo("Restaurant not found: " + restaurantId);
    }

    @Test
    void handleValidation_ShouldReturnBadRequestProblemDetail() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("restaurant", "name", "must not be blank");
        FieldError fieldError2 = new FieldError("restaurant", "email", "must be a valid email");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ProblemDetail problemDetail = exceptionHandler.handleValidation(exception);

        // Then
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation Failed");
        assertThat(problemDetail.getDetail()).contains("name: must not be blank");
        assertThat(problemDetail.getDetail()).contains("email: must be a valid email");
    }

    @Test
    void handleValidation_ShouldHandleSingleFieldError() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("restaurant", "name", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ProblemDetail problemDetail = exceptionHandler.handleValidation(exception);

        // Then
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getDetail()).isEqualTo("name: must not be blank");
    }

    @Test
    void handleRestaurantOrderAccessDenied_ShouldReturnForbiddenProblemDetail() {
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        RestaurantOrderAccessDeniedException exception = new RestaurantOrderAccessDeniedException(orderId, restaurantId);

        ProblemDetail problemDetail = exceptionHandler.handleRestaurantOrderAccessDenied(exception);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Access Denied");
        assertThat(problemDetail.getDetail()).contains(orderId.toString()).contains(restaurantId.toString());
    }

    @Test
    void handleIllegalArgument_ShouldReturnConflictProblemDetail() {
        // Given
        String errorMessage = "A restaurant with email 'test@restaurant.com' already exists";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ProblemDetail problemDetail = exceptionHandler.handleIllegalArgument(exception);

        // Then
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Invalid Request");
        assertThat(problemDetail.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void handleIllegalArgument_ShouldHandleCustomMessage() {
        // Given
        String customMessage = "Custom error message";
        IllegalArgumentException exception = new IllegalArgumentException(customMessage);

        // When
        ProblemDetail problemDetail = exceptionHandler.handleIllegalArgument(exception);

        // Then
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getDetail()).isEqualTo(customMessage);
    }
}
