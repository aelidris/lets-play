package com.letsplay.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Za-z]+(?: [A-Za-z]+){0,3}$", message = "Name must contain only letters with at most 3 spaces")
    @Setter(AccessLevel.NONE)
    private String name;

    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Email(message = "Invalid email format")
    @Pattern(regexp = "^(?=[A-Za-z0-9._-]*[A-Za-z])[A-Za-z0-9](?:[A-Za-z0-9._-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\\.[A-Za-z]{2,6}$", message = "Email format is not valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "Password must include uppercase, lowercase, a number, and a special character")
    private String password;

    @Pattern(regexp = "^(ROLE_USER|ROLE_ADMIN|USER|ADMIN)$", message = "Role must be a valid role")
    private String role;

    @JsonSetter("name")
    public void setName(String name) {
        if (name == null) {
            this.name = null;
            return;
        }
        this.name = name.trim().replaceAll("\\s+", " ");
    }
}