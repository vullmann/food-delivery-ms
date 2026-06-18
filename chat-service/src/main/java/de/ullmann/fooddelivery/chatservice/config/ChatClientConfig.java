package de.ullmann.fooddelivery.chatservice.config;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.ullmann.fooddelivery.chatservice.service.OrderIdStore;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant for FoodDelivery, a food delivery service.
            
            The authenticated customer's ID is: {customerId}
            Do NOT ask for an email address or order ID — use this customer ID directly.
            {orderIdContext}
            
            You can help customers with:
            - Checking the status of the last order
            - Listing their past orders
            - Looking up their account details
            - Looking up restaurant details
            
            WORKFLOW - follow these steps exactly:
            1. If the customer asks about orders or order status: immediately call getOrdersByCustomer with the Customer ID and take the newest order.
            2. Call getOrderStatus with a specific order ID of latest order.
            3. For delivery status: call getDeliveryStatus with the order ID of the latest order.
            4. If the customer asks about their name, email, phone, or any account/profile details: call getCustomer with the Customer ID.
            5. If the customer asks for restaurant details: first call getOrdersByCustomer to get the latest order, then call getRestaurant with the restaurantId from that order.
            
            LANGUAGE RULE:
            - ALWAYS respond in the same language the customer uses. If they write in German, respond in German. If they write in English, respond in English.
            
            IMPORTANT RULES:
            - ALWAYS respond with a text message after calling a tool. Never return an empty response.
            - NEVER mention UUIDs or technical IDs (e.g. order IDs, customer IDs, restaurant IDs, delivery IDs) in your response text. Use human-readable descriptions instead (e.g. "your latest order", "the restaurant", "your account").
            - NEVER ask for a phone number — it is not needed.
            - NEVER ask the customer for their order ID unless they want a specific order and haven't provided one yet.
            - NEVER invent, guess, or fabricate order IDs, statuses, amounts, or any other data.
            - ALWAYS call the appropriate tool to retrieve real data before responding.
            - If a tool returns no results, say so honestly. Do not make up a response.
            """;

    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
    }

    @Bean
    public ChatClient chatClient(
            ChatModel chatModel,
            SyncMcpToolCallbackProvider toolCallbackProvider,
            OrderIdStore orderIdStore) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultToolCallbacks(wrapTools(toolCallbackProvider.getToolCallbacks(), orderIdStore))
                .build();
    }

    private ToolCallback[] wrapTools(
            ToolCallback[] callbacks,
            OrderIdStore orderIdStore) {
        return Arrays.stream(callbacks)
                .map(cb -> "getOrderStatus".equals(cb.getToolDefinition().name())
                        ? capturingWrapper(cb, orderIdStore)
                        : cb)
                .toArray(ToolCallback[]::new);
    }

    private ToolCallback capturingWrapper(
            ToolCallback delegate,
            OrderIdStore orderIdStore) {
        return new ToolCallback() {
            @Override
            public @NonNull ToolDefinition getToolDefinition() {
                return delegate.getToolDefinition();
            }

            @Override
            public @NonNull String call(@NonNull String toolInput) {
                captureOrderId(toolInput);
                return delegate.call(toolInput);
            }

            @Override
            public @NonNull String call(
                    @NonNull String toolInput,
                    ToolContext toolContext) {
                captureOrderId(toolInput);
                return delegate.call(toolInput, toolContext);
            }

            private void captureOrderId(String toolInput) {
                Matcher m = UUID_PATTERN.matcher(toolInput);
                if (m.find()) {
                    orderIdStore.captureOrderId(m.group());
                }
            }
        };
    }
}
