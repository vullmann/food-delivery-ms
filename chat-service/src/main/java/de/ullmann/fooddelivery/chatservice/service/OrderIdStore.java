package de.ullmann.fooddelivery.chatservice.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OrderIdStore {

    private final ThreadLocal<String> currentConversationId = new ThreadLocal<>();
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public void setConversationId(String conversationId) {
        currentConversationId.set(conversationId);
    }

    public void clearConversationId() {
        currentConversationId.remove();
    }

    public void captureOrderId(String orderId) {
        String conversationId = currentConversationId.get();
        if (conversationId != null) {
            cache.putIfAbsent(conversationId, orderId);
        }
    }

    public String get(String conversationId) {
        return cache.get(conversationId);
    }

    public boolean contains(String conversationId) {
        return cache.containsKey(conversationId);
    }
}
