package com.example.dialogflow.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                        .status(status == HttpStatus.OK ? com.example.tendawaks.utils.ResponseStatus.SUCCESS.getStatus() : com.example.tendawaks.utils.ResponseStatus.FAILED.getStatus())
                        .message(message)
                        .msgDeveloper(message)
                        .build());
    }

    public static ResponseEntity<Object> buildResponse(HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status)
                .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                        .status(status == HttpStatus.OK ? com.example.tendawaks.utils.ResponseStatus.SUCCESS.getStatus() : com.example.tendawaks.utils.ResponseStatus.FAILED.getStatus())
                        .message(message)
                        .msgDeveloper(message)
                        .data(data)
                        .build());
    }

    public static ResponseEntity<Object> buildResponse(HttpStatus status, String message,String developerMessage) {
        return ResponseEntity.status(status)
                .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                        .status(status == HttpStatus.OK ? com.example.tendawaks.utils.ResponseStatus.SUCCESS.getStatus() : com.example.tendawaks.utils.ResponseStatus.FAILED.getStatus())
                        .message(message)
                        .msgDeveloper(developerMessage)
                        .build());
    }
}
