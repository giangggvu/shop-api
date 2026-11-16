package io.github.giangggvu.shop.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class Paging {

    public static final int MAX_PAGE_SIZE = 50;

    private Paging() {
    }

    /** Clamps what the client asked for, so one request can never pull the whole table. */
    public static PageRequest of(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
