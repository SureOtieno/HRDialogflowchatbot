package com.example.tendawaks.service;


import com.example.tendawaks.authconfig.AuthenticationResponse;
import com.example.tendawaks.dto.AppUserRequest;
import com.example.tendawaks.model.Role;
import com.example.tendawaks.model.Users;
import com.example.tendawaks.model.repository.UserRepository;
import com.example.tendawaks.utils.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    private JWTService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final AuthenticationConfiguration authenticationConfiguration;

    public AuthenticationManager getAuthenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Override
    public ResponseEntity<Object> register(AppUserRequest appUserRequest) {
        Users appUser = userRepo.findByUsername(appUserRequest.getEmail());
        if (appUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        Users user = Users.builder()
                .email(appUserRequest.getEmail())
                .name(appUserRequest.getName())
                .passwordHash(passwordEncoder.encode(appUserRequest.getPassword()))
                .build();

        user.setUsername(appUserRequest.getEmail());
        user.setCreatedAt(LocalDateTime.now());



        userRepo.save(user);

//        var jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(GenericResponse.builder()
                .data(user)
                .status(HttpStatus.CREATED.value())
                .message("User added successfully")
                .build());
    }

    @Override
    public AuthenticationResponse authenticate(AppUserRequest.AppUserRequestLogin appRequest) throws Exception {

        AuthenticationManager authenticationManager = getAuthenticationManager();
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        appRequest.getUsername(),
                        appRequest.getPassword())

                );

        var user = userRepo.findByUsername(appRequest.getUsername());

        if (user == null) {
            return AuthenticationResponse.builder()
                    .token("User not Found")
                    .build();
        }


        if (!auth.isAuthenticated()) {
            throw new Exception("Unable to authenticate user");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepo.save(user);

        var jwtToken = jwtService.generateToken(user);


        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse handleOAuth2Login(String email, String name, String googleUid) {
        Users user = userRepo.findByUsername(email);

        if (user == null) {
            user = Users.builder()
                    .email(email)
                    .username(email)
                    .googleUid(googleUid)
                    .role(Role.USER)  // or default role you'd prefer
                    .createdAt(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .build();

            userRepo.save(user);
        } else {
            user.setLastLogin(LocalDateTime.now());
            userRepo.save(user);
        }

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


}
