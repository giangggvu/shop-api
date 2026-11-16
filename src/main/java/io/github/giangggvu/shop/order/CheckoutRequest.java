package io.github.giangggvu.shop.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
    @NotBlank @Size(max = 120) String recipientName,
    @NotBlank
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "must be a Vietnamese phone number, such as 0912345678")
    String phone,
    @NotBlank @Size(max = 500) String address,
    @Size(max = 500) String note) {
}
