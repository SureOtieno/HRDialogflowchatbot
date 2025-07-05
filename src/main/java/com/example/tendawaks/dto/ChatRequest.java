package com.example.tendawaks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
public class ChatRequest {

    private String message;
    private String sessionId;
    private String menu;
    private String method;
    private String languageCode;

}

