package com.example.tendawaks.utils;

import lombok.Getter;


@Getter
public enum ResponseStatus {

    SUCCESS(0,"Success"),
    FAILED(-1,"Failed");

    private final int status;
    private final String message;


    ResponseStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
