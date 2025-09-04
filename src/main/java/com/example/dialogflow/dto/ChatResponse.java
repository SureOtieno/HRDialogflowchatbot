package com.example.dialogflow.dto;

import lombok.Getter;

@Getter
public class ChatResponse {
    // Getters
    private boolean success;
    private String reply;
    private String sessionId;
    private String employeeId;
    private String error;

    public ChatResponse(boolean success, String reply, String sessionId, String employeeId, String error) {
        this.success = success;
        this.reply = reply;
        this.sessionId = sessionId;
        this.employeeId = employeeId;
        this.error = error;
    }

}