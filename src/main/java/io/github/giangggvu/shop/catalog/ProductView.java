package io.github.giangggvu.shop.catalog;

public record ProductView(
    Long id,
    String name,
    String slug,
    String description,
    long price,
    int stock,
    String imageUrl,
    boolean active,
    CategoryView category) {

    public static ProductView from(Product product) {
        return new ProductView(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getImageUrl(),
            product.isActive(),
            CategoryView.from(product.getCategory()));
    }
}
