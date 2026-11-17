package io.github.giangggvu.shop.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 255) String email,
    
    @NotBlank @Size(min = 8, max = 64) String password,
    @NotBlank @Size(max = 120) String fullName) {
}
