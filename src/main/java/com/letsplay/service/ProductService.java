package com.letsplay.service;

import com.letsplay.dto.ProductRequest;
import com.letsplay.dto.ProductResponse;
import com.letsplay.exception.ResourceNotFoundException;
import com.letsplay.model.Product;
import com.letsplay.model.User;
import com.letsplay.repository.ProductRepository;
import com.letsplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = findProductOrThrow(id);
        return mapToResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request, String userEmail) {
        User user = findUserByEmailOrThrow(userEmail);

        Product product = Product.builder()
                .name(normalizeSpaces(request.getName()))
                .description(normalizeSpaces(request.getDescription()))
                .price(request.getPrice())
                .userId(user.getId())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    public ProductResponse updateProduct(String id, ProductRequest request, String userEmail, boolean isAdmin) {
        Product product = findProductAndVerifyAccess(id, userEmail, isAdmin, "update");

        product.setName(normalizeSpaces(request.getName()));
        product.setDescription(normalizeSpaces(request.getDescription()));
        product.setPrice(request.getPrice());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    public void deleteProduct(String id, String userEmail, boolean isAdmin) {
        Product product = findProductAndVerifyAccess(id, userEmail, isAdmin, "delete");
        productRepository.delete(product);
    }

    private Product findProductOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Product findProductAndVerifyAccess(String id, String userEmail, boolean isAdmin, String action) {
        Product product = findProductOrThrow(id);
        User user = findUserByEmailOrThrow(userEmail);

        if (!isAdmin && !product.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to " + action + " this product");
        }
        return product;
    }

    private String normalizeSpaces(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .userId(product.getUserId())
                .build();
    }
}