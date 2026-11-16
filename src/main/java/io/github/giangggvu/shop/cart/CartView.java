package io.github.giangggvu.shop.cart;

import io.github.giangggvu.shop.catalog.Product;
import java.util.List;

public record CartView(List<Line> items, long total, int itemCount) {

    public record Line(
        Long productId,
        String name,
        String slug,
        String imageUrl,
        long unitPrice,
        int quantity,
        long lineTotal,
        int stock) {

        static Line from(CartItem item) {
            Product product = item.getProduct();
            return new Line(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getImageUrl(),
                product.getPrice(),
                item.getQuantity(),
                product.getPrice() * item.getQuantity(),
                product.getStock());
        }
    }

    public static CartView empty() {
        return new CartView(List.of(), 0, 0);
    }

    public static CartView from(Cart cart) {
        List<Line> lines = cart.getItems().stream().map(Line::from).toList();
        long total = lines.stream().mapToLong(Line::lineTotal).sum();
        int itemCount = lines.stream().mapToInt(Line::quantity).sum();
        return new CartView(lines, total, itemCount);
    }
}
