package com.letsplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 5, message = "Product name must be at least 5 characters long")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(min = 20, message = "Product description must be at least 20 characters long")
    private String description;

    @NotNull(message = "Price is required")
    private Double price;
}