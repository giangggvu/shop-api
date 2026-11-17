package io.github.giangggvu.shop.common;

import java.util.List;
import org.springframework.data.domain.Page;


public record PageView<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageView<T> of(Page<T> page) {
        return new PageView<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
