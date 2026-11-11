package com.letsplay.config;

import com.letsplay.model.User;
import com.letsplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> "ROLE_ADMIN".equals(user.getRole()));

        if (!adminExists) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@letsplay.com");
            admin.setPassword(passwordEncoder.encode("AdminSecurePassword123!"));
            admin.setRole("ROLE_ADMIN");
            
            userRepository.save(admin);
            System.out.println(">>> Default Admin account created successfully! (Email: admin@letsplay.com, Password: AdminSecurePassword123!)");
        }
    }
}