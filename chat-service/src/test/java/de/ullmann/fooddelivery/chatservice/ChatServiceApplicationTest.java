package de.ullmann.fooddelivery.chatservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ChatServiceApplicationTest {

    @Test
    void main_shouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(null);

            ChatServiceApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(ChatServiceApplication.class, new String[]{}));
        }
    }
}
