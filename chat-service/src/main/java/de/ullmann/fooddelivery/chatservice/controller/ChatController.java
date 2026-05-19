package de.ullmann.fooddelivery.chatservice.controller;

import de.ullmann.fooddelivery.chatservice.dto.ChatRequest;
import de.ullmann.fooddelivery.chatservice.dto.ChatResponse;
import de.ullmann.fooddelivery.chatservice.exception.UnauthorizedException;
import de.ullmann.fooddelivery.chatservice.service.ChatService;
import de.ullmann.fooddelivery.chatservice.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final JwtService jwtService;

    public ChatController(ChatService chatService, JwtService jwtService) {
        this.chatService = chatService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid ChatRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String customerId = jwtService.extractCustomerId(authHeader.substring(7));
        return ResponseEntity.ok(chatService.chat(request.sessionId(), request.message(), customerId));
    }
}
