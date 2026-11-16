package io.github.giangggvu.shop.cart;

/** Only the ids and quantities in a cart, without loading any product. Checkout locks the products itself. */
public record CartLineRef(Long productId, Integer quantity) {
}
