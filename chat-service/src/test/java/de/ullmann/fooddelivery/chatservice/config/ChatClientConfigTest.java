package de.ullmann.fooddelivery.chatservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import de.ullmann.fooddelivery.chatservice.service.OrderIdStore;

class ChatClientConfigTest {

    private ChatClientConfig config;

    @BeforeEach
    void setUp() {
        config = new ChatClientConfig();
    }

    // ------------------------------------------------------------------
    // chatMemory bean
    // ------------------------------------------------------------------

    @Test
    void chatMemory_shouldReturnNonNullChatMemory() {
        ChatMemory memory = config.chatMemory();

        assertThat(memory).isNotNull();
    }

    // ------------------------------------------------------------------
    // localChatClient / awsChatClient beans
    // ------------------------------------------------------------------

    @Test
    void localChatClient_shouldReturnNonNullChatClient() {
        ChatModel model = mock(ChatModel.class);
        SyncMcpToolCallbackProvider provider = mock(SyncMcpToolCallbackProvider.class);
        when(provider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        ChatClient client = config.localChatClient(model, provider, new OrderIdStore());

        assertThat(client).isNotNull();
    }

    @Test
    void awsChatClient_shouldReturnNonNullChatClient() {
        ChatModel model = mock(ChatModel.class);
        SyncMcpToolCallbackProvider provider = mock(SyncMcpToolCallbackProvider.class);
        when(provider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        ChatClient client = config.awsChatClient(model, provider, new OrderIdStore());

        assertThat(client).isNotNull();
    }

    // ------------------------------------------------------------------
    // wrapTools: non-getOrderStatus callbacks pass through unchanged
    // ------------------------------------------------------------------

    @Test
    void wrapTools_shouldPassThroughNonOrderStatusCallbacks() throws Exception {
        ToolCallback other = mockCallback("getCustomer");
        OrderIdStore store = new OrderIdStore();

        ToolCallback[] result = invokeWrapTools(new ToolCallback[]{other}, store);

        assertThat(result[0]).isSameAs(other);
    }

    // ------------------------------------------------------------------
    // wrapTools: getOrderStatus callback is wrapped — delegates definition
    // ------------------------------------------------------------------

    @Test
    void wrapTools_shouldWrapGetOrderStatusCallback() throws Exception {
        ToolCallback delegate = mockCallback("getOrderStatus");
        ToolDefinition def = delegate.getToolDefinition();
        OrderIdStore store = new OrderIdStore();

        ToolCallback[] result = invokeWrapTools(new ToolCallback[]{delegate}, store);

        assertThat(result[0]).isNotSameAs(delegate);
        assertThat(result[0].getToolDefinition()).isSameAs(def);
    }

    // ------------------------------------------------------------------
    // capturingWrapper.call(input) — UUID found → captured
    // ------------------------------------------------------------------

    @Test
    void capturingWrapper_call_shouldCaptureUuidWhenPresentInInput() throws Exception {
        String uuid = "12345678-1234-1234-1234-123456789012";
        ToolCallback delegate = mockCallback("getOrderStatus");
        when(delegate.call("{\"orderId\":\"" + uuid + "\"}")).thenReturn("ok");
        OrderIdStore store = new OrderIdStore();
        store.setConversationId("conv-1");

        ToolCallback[] result = invokeWrapTools(new ToolCallback[]{delegate}, store);
        result[0].call("{\"orderId\":\"" + uuid + "\"}");

        assertThat(store.get("conv-1")).isEqualTo(uuid);
    }

    // ------------------------------------------------------------------
    // capturingWrapper.call(input) — no UUID → nothing captured
    // ------------------------------------------------------------------

    @Test
    void capturingWrapper_call_shouldNotCaptureWhenNoUuidInInput() throws Exception {
        ToolCallback delegate = mockCallback("getOrderStatus");
        when(delegate.call("{}")).thenReturn("ok");
        OrderIdStore store = new OrderIdStore();
        store.setConversationId("conv-2");

        ToolCallback[] result = invokeWrapTools(new ToolCallback[]{delegate}, store);
        result[0].call("{}");

        assertThat(store.get("conv-2")).isNull();
    }

    // ------------------------------------------------------------------
    // capturingWrapper.call(input, toolContext) — UUID found → captured
    // ------------------------------------------------------------------

    @Test
    void capturingWrapper_callWithContext_shouldCaptureUuid() throws Exception {
        String uuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        ToolCallback delegate = mockCallback("getOrderStatus");
        ToolContext ctx = mock(ToolContext.class);
        when(delegate.call("{\"id\":\"" + uuid + "\"}", ctx)).thenReturn("ctx-ok");
        OrderIdStore store = new OrderIdStore();
        store.setConversationId("conv-3");

        ToolCallback[] result = invokeWrapTools(new ToolCallback[]{delegate}, store);
        String callResult = result[0].call("{\"id\":\"" + uuid + "\"}", ctx);

        assertThat(callResult).isEqualTo("ctx-ok");
        assertThat(store.get("conv-3")).isEqualTo(uuid);
    }

    // ------------------------------------------------------------------
    // capturingWrapper.call(input, toolContext) — no UUID → nothing captured
    // ------------------------------------------------------------------

    @Test
    void capturingWrapper_callWithContext_shouldNotCaptureWhenNoUuid() throws Exception {
        ToolCallback delegate = mockCallback("getOrderStatus");
        ToolContext ctx = mock(ToolContext.class);
        when(delegate.call("{}", ctx)).thenReturn("empty");
        OrderIdStore store = new OrderIdStore();
        store.setConversationId("conv-4");

        ToolCallback[] result = invokeWrapTools(new ToolCallback[]{delegate}, store);
        result[0].call("{}", ctx);

        assertThat(store.get("conv-4")).isNull();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private ToolCallback mockCallback(String name) {
        ToolCallback cb = mock(ToolCallback.class);
        ToolDefinition def = mock(ToolDefinition.class);
        when(def.name()).thenReturn(name);
        when(cb.getToolDefinition()).thenReturn(def);
        return cb;
    }

    @SuppressWarnings("unchecked")
    private ToolCallback[] invokeWrapTools(
            ToolCallback[] callbacks,
            OrderIdStore store) throws Exception {
        Method method = ChatClientConfig.class.getDeclaredMethod(
                "wrapTools", ToolCallback[].class, OrderIdStore.class);
        method.setAccessible(true);
        return (ToolCallback[]) method.invoke(config, callbacks, store);
    }
}
