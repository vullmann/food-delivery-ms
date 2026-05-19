package de.ullmann.fooddelivery.notificationservice.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        ReflectionTestUtils.setField(notificationService, "from", "whatsapp:+14155238886");
    }

    @Test
    void send_success_shouldNotThrow() {
        MessageCreator creator = mock(MessageCreator.class);
        when(creator.create()).thenReturn(mock(Message.class));

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(String.class)))
                    .thenReturn(creator);

            notificationService.send("+49123456789", "Hello!");
        }
    }

    @Test
    void send_failure_shouldNotPropagateException() {
        MessageCreator creator = mock(MessageCreator.class);
        when(creator.create()).thenThrow(new RuntimeException("Twilio error"));

        try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(String.class)))
                    .thenReturn(creator);

            notificationService.send("+49123456789", "Hello!");
        }
    }
}
