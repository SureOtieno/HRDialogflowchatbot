package com.example.dialogflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepseekService {
    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";

    public String getCompletion(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", userMessage)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(DEEPSEEK_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            return ((Map<String, String>) ((Map<String, Object>)
                    ((List<?>) body.get("choices")).get(0)).get("message")).get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I had an issue talking to DeepSeek.";
        }
    }
}

