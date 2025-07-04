package com.example.tendawaks.dto;

import lombok.Getter;

@Getter
public class ChatResponse {
    // Getters
    private boolean success;
    private String reply;
    private String sessionId;
    private String error;

    public ChatResponse(boolean success, String reply, String sessionId, String error) {
        this.success = success;
        this.reply = reply;
        this.sessionId = sessionId;
        this.error = error;
    }

}