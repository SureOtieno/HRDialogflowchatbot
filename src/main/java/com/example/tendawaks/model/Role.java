package com.example.tendawaks.model;

public enum Role {
    USER("ROLE_USER"),
    CONTRACTOR("ROLE_CONTRACTOR", "ROLE_USER"),
    ADMIN("ROLE_ADMIN", "ROLE_USER"), // Inherits USER privileges
    SUPER_ADMIN("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_USER");

    private final String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }

}
