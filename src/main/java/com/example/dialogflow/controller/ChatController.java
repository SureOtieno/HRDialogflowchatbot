package com.example.dialogflow.controller;

import com.example.dialogflow.service.OpenAiService;
import com.example.dialogflow.utils.constants.Constants;
import com.example.dialogflow.dto.ChatRequest;
import com.example.dialogflow.dto.ChatResponse;
import com.example.dialogflow.service.DialogflowService;
import com.example.tendawaks.utils.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.UUID;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final DialogflowService dialogflowService;
    private final OpenAiService openAiService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_API_KEY = "sk-..."; // 🔒 Keep it safe
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public ChatController(DialogflowService dialogflowService, OpenAiService openAiService) {
        this.dialogflowService = dialogflowService;
        this.openAiService = openAiService;
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

    @PostMapping("/webhook")
    public ResponseEntity<Object> webhook(@RequestBody Map<String, Object> payload) {
        try {
            String intentName = ((Map<String, Object>) payload.get("queryResult")).get("intent").toString();
            String userInput = ((Map<String, Object>) payload.get("queryResult")).get("queryText").toString();

            // Route logic based on intent
            switch (intentName) {
                case "TellMeAJoke":
                case "AskOpenAI":
                    // Offload to GPT
                    String aiResponse = openAiService.getCompletion(userInput);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                                    .status(ResponseStatus.SUCCESS.getStatus())
                                    .message(Constants.SUCCESS)
                                    .msgDeveloper(Constants.SUCCESS)
                                    .data(aiResponse)
                                    .build());


                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                                    .status(ResponseStatus.FAILED.getStatus())
                                    .message("Sorry, I had an issue getting response")
                                    .msgDeveloper(Constants.FAILED)
                                    .build());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                            .status(ResponseStatus.FAILED.getStatus())
                            .message(Constants.FAILED)
                            .msgDeveloper(Constants.FAILED)
                            .build());
        }
    }

}