package de.ullmann.fooddelivery.chatservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.chatservice.dto.ChatRequest;
import de.ullmann.fooddelivery.chatservice.dto.ChatResponse;
import de.ullmann.fooddelivery.chatservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.chatservice.exception.UnauthorizedException;
import de.ullmann.fooddelivery.chatservice.service.ChatService;
import de.ullmann.fooddelivery.chatservice.service.JwtService;

@WebMvcTest(ChatController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "spring.jpa.open-in-view=false")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtService jwtService;

    // ------------------------------------------------------------------
    // Happy path
    // ------------------------------------------------------------------

    @Test
    void chat_shouldReturn200WhenValidBearerToken() throws Exception {
        when(jwtService.extractCustomerId("valid-token")).thenReturn("customer-uuid");
        when(chatService.chat(eq("sess-1"), eq("hello"), eq("customer-uuid")))
                .thenReturn(new ChatResponse("sess-1", "Hi there!"));

        mockMvc.perform(post("/chat")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("sess-1", "hello"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-1"))
                .andExpect(jsonPath("$.reply").value("Hi there!"));
    }

    // ------------------------------------------------------------------
    // Missing or invalid Authorization header
    // ------------------------------------------------------------------

    @Test
    void chat_shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(null, "hello"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chat_shouldReturn401WhenAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
        mockMvc.perform(post("/chat")
                        .header("Authorization", "Token abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(null, "hello"))))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // JWT validation failure
    // ------------------------------------------------------------------

    @Test
    void chat_shouldReturn401WhenJwtIsInvalid() throws Exception {
        when(jwtService.extractCustomerId(any()))
                .thenThrow(new UnauthorizedException("Invalid or expired token"));

        mockMvc.perform(post("/chat")
                        .header("Authorization", "Bearer bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(null, "hi"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid or expired token"));
    }

    // ------------------------------------------------------------------
    // Request validation
    // ------------------------------------------------------------------

    @Test
    void chat_shouldReturn400WhenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/chat")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(null, ""))))
                .andExpect(status().isBadRequest());
    }
}
