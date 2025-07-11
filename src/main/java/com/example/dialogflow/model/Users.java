package com.example.dialogflow.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@Table(name = "app_user", schema = "user_service")
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(
            name = "app_user_seq",
            sequenceName = "user_service.app_user_id_seq",
            allocationSize = 1
    )
    @Column(name = "id")
    private int id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "google_uid", unique = true)
    private String googleUid;

    @Column(name = "location")
    private String location;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "VARCHAR(20)")
    private Role role = Role.USER;

    @Column(name = "is_active")
    private boolean isActive = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt = LocalDateTime.now();

    // Ensure role can never be null
    @PrePersist @PreUpdate
    private void validateRole() {
        if (this.role == null) {
            role = Role.USER;
        }
    }

}
