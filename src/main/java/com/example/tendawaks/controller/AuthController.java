package com.example.tendawaks.controller;


import com.google.api.client.json.jackson2.JacksonFactory;
import com.example.tendawaks.authconfig.AuthenticationResponse;
import com.example.tendawaks.dto.AppUserRequest;
import com.example.tendawaks.service.UsersService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsersService appUserService;

    @PostMapping("/signup")
    public ResponseEntity<Object> createUser(@RequestBody AppUserRequest appUserRequest) {
        try {
            appUserService.register(appUserRequest);  // ← actually call your service!
            return ResponseEntity.ok(Map.of("message", "User created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AppUserRequest.AppUserRequestLogin request) throws Exception {
        try{

            return ResponseEntity.ok(appUserService.authenticate(request));

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials")); // JSON error
        }

    }
    @PostMapping("/auth/google")
    public ResponseEntity<AuthenticationResponse> googleLogin(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        GoogleIdToken.Payload payload = verifyGoogleToken(token);
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleUid = payload.getSubject();

        AuthenticationResponse response = appUserService.handleOAuth2Login(email, name, googleUid);

        return ResponseEntity.ok(response);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList("YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com"))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            return (idToken != null) ? idToken.getPayload() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
