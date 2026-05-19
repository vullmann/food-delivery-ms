package de.ullmann.fooddelivery.chatservice.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final OrderIdStore orderIdStore;

    private static final String FALLBACK_MESSAGE = "Sorry, I didn't get a response. Please try again.";

    public ChatService(
            ChatClient chatClient,
            ChatMemory chatMemory,
            OrderIdStore orderIdStore) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.orderIdStore = orderIdStore;
    }

    public de.ullmann.fooddelivery.chatservice.dto.ChatResponse chat(
            String sessionId,
            String message,
            String customerId) {
        String conversationId = (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();

        String cachedOrderId = orderIdStore.get(conversationId);
        String orderIdContext = cachedOrderId != null
                ? "The customer's latest order ID is already known: " + cachedOrderId +
                  ". When you only need the order ID (e.g. to call getOrderStatus or getDeliveryStatus), use it directly without calling getOrdersByCustomer first. If the customer asks about order contents or details, you may still call getOrdersByCustomer."
                : "The customer's latest order ID is not yet known.";

        orderIdStore.setConversationId(conversationId);
        try {
            ChatResponse chatResponse = chatClient.prompt()
                    .system(s -> s
                            .param("customerId", customerId)
                            .param("orderIdContext", orderIdContext))
                    .user(message)
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(conversationId)
                            .build())
                    .call()
                    .chatResponse();

            String reply = extractText(chatResponse);
            return new de.ullmann.fooddelivery.chatservice.dto.ChatResponse(conversationId, reply);
        } finally {
            orderIdStore.clearConversationId();
        }
    }

    private String extractText(ChatResponse chatResponse) {
        if (chatResponse == null) {
            log.warn("chatResponse is null");
            return FALLBACK_MESSAGE;
        }

        for (Generation g : chatResponse.getResults()) {
            String text = g.getOutput().getText();
            if (text != null && !text.isBlank()) {
                return text;
            }
        }

        log.warn("All generations had null/blank text. Results count: {}. FinishReasons: {}",
                chatResponse.getResults().size(),
                chatResponse.getResults().stream()
                        .map(g -> g.getMetadata().getFinishReason())
                        .toList());

        return FALLBACK_MESSAGE;
    }
}
