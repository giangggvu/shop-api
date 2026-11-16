package io.github.giangggvu.shop.catalog;

import io.github.giangggvu.shop.common.BadRequestException;
import java.util.Locale;
import org.springframework.data.domain.Sort;

public enum ProductSort {
    // The id tie-breaker keeps the order stable when two products share a price or creation time.
    NEWEST(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    PRICE_ASC(Sort.by(Sort.Order.asc("price"), Sort.Order.asc("id"))),
    PRICE_DESC(Sort.by(Sort.Order.desc("price"), Sort.Order.asc("id")));

    private final Sort sort;

    ProductSort(Sort sort) {
        this.sort = sort;
    }

    public Sort sort() {
        return sort;
    }

    public static ProductSort parse(String value) {
        if (value == null || value.isBlank()) {
            return NEWEST;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown sort '" + value + "'. Use newest, price_asc or price_desc");
        }
    }
}
