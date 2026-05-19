package de.ullmann.fooddelivery.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${twilio.whatsapp-from}")
    private String from;

    public void send(String phone, String text) {
        String to = "whatsapp:" + phone;
        try {
            Message.creator(new PhoneNumber(to), new PhoneNumber(from), text).create();
            log.info("WhatsApp sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp to {}: {}", to, e.getMessage());
        }
    }
}
