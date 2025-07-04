package com.example.tendawaks.controller;

import com.example.tendawaks.dto.ChatRequest;
import com.example.tendawaks.dto.ChatResponse;
import com.example.tendawaks.service.DialogflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final DialogflowService dialogflowService;

    public ChatController(DialogflowService dialogflowService) {
        this.dialogflowService = dialogflowService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : UUID.randomUUID().toString();

            logger.info("Processing chat request - Session: {}, Message: {}", sessionId, request.getMessage());

            String responseText = dialogflowService.detectIntent(request.getMessage(), sessionId);

            ChatResponse response = new ChatResponse(
                    true,
                    responseText,
                    sessionId,
                    null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing chat request", e);

            ChatResponse errorResponse = new ChatResponse(
                    false,
                    "Sorry, I encountered an error processing your request",
                    request.getSessionId(),
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}