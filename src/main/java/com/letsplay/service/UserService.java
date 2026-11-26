package com.letsplay.service;

import com.letsplay.dto.RegisterRequest;
import com.letsplay.dto.UserResponse;
import com.letsplay.exception.DuplicateEmailException;
import com.letsplay.exception.ResourceNotFoundException;
import com.letsplay.model.User;
import com.letsplay.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    public UserResponse updateUser(String id, RegisterRequest request, boolean isAdmin, String currentUserEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        if (!isAdmin && !user.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this user profile");
        }

        if (request.getEmail() != null && !user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already in use");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (isAdmin && request.getRole() != null && !request.getRole().isEmpty()) {
            String normalizedRequested = normalizeRole(request.getRole());
            String normalizedCurrent = normalizeRole(user.getRole());

            if (user.getId().equals(currentUser.getId()) && !normalizedRequested.equals(normalizedCurrent)) {
                throw new AccessDeniedException("Admins cannot change their own role");
            }
            user.setRole(normalizedRequested);
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String cleaned = role.trim().toUpperCase();
        return cleaned.startsWith("ROLE_") ? cleaned : "ROLE_" + cleaned;
    }

    public void deleteUser(String id, String currentUserEmail) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Admins cannot delete their own account");
        }

        userRepository.delete(targetUser);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}