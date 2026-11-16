package io.github.giangggvu.shop.catalog;

/** What a shopper filters the catalogue by. Every field except page and size is optional. */
public record ProductQuery(String category, String q, Long minPrice, Long maxPrice, String sort, int page, int size) {
}
