package io.github.giangggvu.shop.cart;

import io.github.giangggvu.shop.common.CurrentUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearer")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartView get(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(CurrentUser.id(jwt));
    }

    @PostMapping("/items")
    public CartView add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(CurrentUser.id(jwt), request);
    }

    @PutMapping("/items/{productId}")
    public CartView update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateItemRequest request) {
        return cartService.updateItem(CurrentUser.id(jwt), productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public CartView remove(@AuthenticationPrincipal Jwt jwt, @PathVariable("productId") Long productId) {
        return cartService.removeItem(CurrentUser.id(jwt), productId);
    }
}
