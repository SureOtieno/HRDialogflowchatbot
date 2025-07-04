package com.example.tendawaks.dto;


import com.example.tendawaks.model.Role;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
public class AppUserRequest {
    @NonNull
    private String password;

    @NonNull
    private String email;

    @NonNull
    private String name;

    @NonNull
    private String location;


    @Data
    @Builder
    @RequiredArgsConstructor
    public static class AppUserRequestLogin {
        @NonNull
        private String password;

        @NonNull
        private String username;


    }

    @Data
    @RequiredArgsConstructor
    public static class resetPasswordRequest {
        @NonNull
        private String username;

        @NonNull
        private String password;

    }

    @Data
    @RequiredArgsConstructor
    public static class forgotenPasswordRequest {
        @NonNull
        private String username;
    }

}
