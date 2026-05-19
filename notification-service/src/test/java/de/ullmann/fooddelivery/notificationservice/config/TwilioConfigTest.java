package de.ullmann.fooddelivery.notificationservice.config;

import com.twilio.Twilio;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class TwilioConfigTest {

    @Test
    void init_shouldCallTwilioInit() {
        TwilioConfig config = new TwilioConfig();
        ReflectionTestUtils.setField(config, "accountSid", "ACtest123");
        ReflectionTestUtils.setField(config, "authToken", "authtest456");

        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class)) {
            ReflectionTestUtils.invokeMethod(config, "init");
            twilioMock.verify(() -> Twilio.init(anyString(), anyString()), times(1));
        }
    }
}
