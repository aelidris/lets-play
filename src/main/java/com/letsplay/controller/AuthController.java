package com.letsplay.controller;

import com.letsplay.dto.AuthResponse;
import com.letsplay.dto.LoginRequest;
import com.letsplay.dto.RegisterRequest;
import com.letsplay.dto.UserResponse;
import com.letsplay.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse response = authService.register(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Email is already in use")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}