package com.letsplay.service;

import com.letsplay.dto.AuthResponse;
import com.letsplay.dto.LoginRequest;
import com.letsplay.dto.RegisterRequest;
import com.letsplay.dto.UserResponse;
import com.letsplay.model.User;
import com.letsplay.repository.UserRepository;
import com.letsplay.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        String role = (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) 
                ? "ROLE_ADMIN" : "ROLE_USER";

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        
        String token = jwtUtils.generateToken(userDetails);
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return new AuthResponse(token, userDetails.getUsername(), role);
    }
}