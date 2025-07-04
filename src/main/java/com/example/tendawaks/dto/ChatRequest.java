package com.example.tendawaks.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
public class ChatRequest {
    private String message;
    private String sessionId;

}
