package io.github.giangggvu.shop.catalog;

public record CategoryView(Long id, String name, String slug) {

    public static CategoryView from(Category category) {
        return new CategoryView(category.getId(), category.getName(), category.getSlug());
    }
}
