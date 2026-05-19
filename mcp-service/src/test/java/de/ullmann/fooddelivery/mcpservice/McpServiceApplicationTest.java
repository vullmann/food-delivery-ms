package de.ullmann.fooddelivery.mcpservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class McpServiceApplicationTest {

    @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            mockedStatic.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            McpServiceApplication.main(new String[]{});

            mockedStatic.verify(() -> SpringApplication.run(eq(McpServiceApplication.class), any(String[].class)));
        }
    }
}
