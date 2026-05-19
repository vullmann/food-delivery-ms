package de.ullmann.fooddelivery.apigateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ApiGatewayApplicationTest {

    @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            mockedStatic.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            ApiGatewayApplication.main(new String[]{});

            mockedStatic.verify(() -> SpringApplication.run(eq(ApiGatewayApplication.class), any(String[].class)));
        }
    }
}
