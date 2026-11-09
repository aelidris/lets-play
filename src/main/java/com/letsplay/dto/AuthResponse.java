package com.letsplay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;

    public AuthResponse(String token, String email, String role) {
        this.token = token;
    }
}