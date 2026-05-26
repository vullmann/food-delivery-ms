package de.ullmann.fooddelivery.chatservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    private static final String FALLBACK_MESSAGE = "Sorry, I didn't get a response. Please try again.";

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private OrderIdStore orderIdStore;

    private ChatService chatService;

    @BeforeEach
    void setUp() {

        String systemTemplateBlueprint =
                "Customer: {customerId}. Context details: {orderIdContext}";

        // Build a real ChatClient wrapping the mocked engine
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemTemplateBlueprint)
                .build();

        // Construct the service using the real client
        chatService = new ChatService(chatClient, chatMemory, orderIdStore);
    }

    // ------------------------------------------------------------------
    // Helper: Simple, stable stub targeting the boundary (The Prompt)
    // ------------------------------------------------------------------
    private void stubChain(ChatResponse response) {
        // No matter what methods are chained (.system, .user, .advisors),
        // ChatClient always calls chatModel.call(Prompt) at the very end.
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
    }


    // ------------------------------------------------------------------
    // sessionId handling
    // ------------------------------------------------------------------

    @Test
    void chat_shouldUseProvidedSessionId() {
        stubChain(mockAiResponse("Hello!"));

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("my-session", "hi", "cust-1");

        assertThat(result.sessionId()).isEqualTo("my-session");
        assertThat(result.reply()).isEqualTo("Hello!");
    }


    @Test
    void chat_shouldGenerateConversationIdWhenSessionIdIsNull() {
        stubChain(mockAiResponse("Hi!"));

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat(null, "hello", "cust-2");

        assertThat(result.sessionId()).isNotNull().isNotBlank();
    }

    @Test
    void chat_shouldGenerateConversationIdWhenSessionIdIsBlank() {
        stubChain(mockAiResponse("Moin!"));

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("   ", "hello", "cust-3");

        assertThat(result.sessionId()).isNotEqualTo("   ");
    }


    // ------------------------------------------------------------------
    // orderIdContext branches
    // ------------------------------------------------------------------

    @Test
    void chat_shouldBuildContextWhenOrderIdIsCached() {
        // 1. Setup the fake cache hit
        when(orderIdStore.get(anyString())).thenReturn("cached-order-uuid");
        stubChain(mockAiResponse("Your order is on the way"));

        // 2. Run the real code execution path
        chatService.chat("session-x", "status?", "cust");

        // 3. Capture the actual package sent to the AI engine
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        // 4. THE REAL TEST: Proves the code actually embedded the data into the prompt!
        String textSentToAi = promptCaptor.getValue().getContents();
        assertThat(textSentToAi).contains("cached-order-uuid");
    }

    @Test
    void chat_shouldBuildUnknownContextWhenOrderIdIsNotCached() {
        // 1. Setup the fake cache hit
        when(orderIdStore.get(anyString())).thenReturn(null);
        stubChain(mockAiResponse("Your order is on the way"));

        // 2. Run the real code execution path
        chatService.chat("session-x", "status?", "cust");

        // 3. Capture the actual package sent to the AI engine
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        // 4. THE REAL TEST: Proves the code actually embedded the data into the prompt!
        String textSentToAi = promptCaptor.getValue().getContents();
        assertThat(textSentToAi).contains("latest order ID is not yet know");
    }


    // ------------------------------------------------------------------
    // finally block
    // ------------------------------------------------------------------

    @Test
    void chat_shouldAlwaysClearConversationId() {
        stubChain(mockAiResponse("Done"));

        chatService.chat("s1", "msg", "cust");

        verify(orderIdStore).clearConversationId();
    }

    // ------------------------------------------------------------------
    // extractText branches
    // ------------------------------------------------------------------


    @Test
    void chat_shouldReturnFallbackWhenChatResponseIsNull() {
        stubChain(null);

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("s", "msg", "cust");

        assertThat(result.reply()).isEqualTo(FALLBACK_MESSAGE);
    }


    @Test
    void chat_shouldReturnFallbackWhenAllGenerationsHaveBlankText() {
        AssistantMessage output = mock(AssistantMessage.class);
        when(output.getText()).thenReturn("   ");

        Generation gen = mock(Generation.class);
        ChatGenerationMetadata metadata = mock(ChatGenerationMetadata.class);
        when(gen.getMetadata()).thenReturn(metadata);
        when(gen.getOutput()).thenReturn(output);

        ChatResponse aiResp = mock(ChatResponse.class);
        when(aiResp.getResults()).thenReturn(List.of(gen));
        stubChain(aiResp);

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("s", "msg", "cust");

        assertThat(result.reply()).isEqualTo(FALLBACK_MESSAGE);
    }

    @Test
    void chat_shouldReturnFallbackWhenAllGenerationsHaveNullText() {
        // 1. Explicitly mock the outer layer and its internal data container
        Generation gen = mock(Generation.class);
        ChatGenerationMetadata metadata = mock(ChatGenerationMetadata.class);
        when(gen.getMetadata()).thenReturn(metadata);

        AssistantMessage output = mock(AssistantMessage.class);


        // 2. Setup the explicit, single-level stubbing chain
        when(gen.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(null);

        ChatResponse aiResp = mock(ChatResponse.class);
        when(aiResp.getResults()).thenReturn(List.of(gen));
        stubChain(aiResp);

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("s", "msg", "cust");

        assertThat(result.reply()).isEqualTo("Sorry, I didn't get a response. Please try again.");
    }
            /*
    @Test
    void chat_shouldReturnFallbackWhenAllGenerationsHaveBlankText() {
        Generation gen = mock(Generation.class, Answers.RETURNS_DEEP_STUBS);
        when(gen.getOutput().getText()).thenReturn("   ");

        ChatResponse aiResp = mock(ChatResponse.class);
        when(aiResp.getResults()).thenReturn(List.of(gen));
        stubChain(aiResp);

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("s", "msg", "cust");

        assertThat(result.reply()).isEqualTo("Sorry, I didn't get a response. Please try again.");
    }

    @Test
    void chat_shouldReturnFirstNonBlankGenerationText() {
        Generation blank = mock(Generation.class, Answers.RETURNS_DEEP_STUBS);
        when(blank.getOutput().getText()).thenReturn("");

        AssistantMessage msg = mock(AssistantMessage.class);
        when(msg.getText()).thenReturn("Good answer");
        Generation good = mock(Generation.class);
        when(good.getOutput()).thenReturn(msg);

        ChatResponse aiResp = mock(ChatResponse.class);
        when(aiResp.getResults()).thenReturn(List.of(blank, good));
        stubChain(aiResp);

        de.ullmann.fooddelivery.chatservice.dto.ChatResponse result =
                chatService.chat("s", "msg", "cust");

        assertThat(result.reply()).isEqualTo("Good answer");
    }
         */


    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private ChatResponse mockAiResponse(String text) {
        AssistantMessage msg = mock(AssistantMessage.class);
        when(msg.getText()).thenReturn(text);
        Generation gen = mock(Generation.class);
        when(gen.getOutput()).thenReturn(msg);
        ChatResponse resp = mock(ChatResponse.class);
        when(resp.getResults()).thenReturn(List.of(gen));
        return resp;
    }
}
