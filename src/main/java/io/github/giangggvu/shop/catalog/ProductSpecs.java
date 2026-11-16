package io.github.giangggvu.shop.catalog;

import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Small filters that the catalogue search combines depending on which parameters were sent. */
final class ProductSpecs {

    private ProductSpecs() {
    }

    static Specification<Product> active() {
        return (root, query, cb) -> cb.isTrue(root.<Boolean>get("active"));
    }

    static Specification<Product> inCategory(String categorySlug) {
        return (root, query, cb) -> cb.equal(root.get("category").get("slug"), categorySlug);
    }

    static Specification<Product> nameContains(String keyword) {
        String pattern = "%" + escapeLike(keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("name")), pattern, '\\');
    }

    static Specification<Product> priceAtLeast(long min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<Long>get("price"), min);
    }

    static Specification<Product> priceAtMost(long max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<Long>get("price"), max);
    }

    /** A search for "100%" should look for the text "100%", not treat % as a wildcard. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
