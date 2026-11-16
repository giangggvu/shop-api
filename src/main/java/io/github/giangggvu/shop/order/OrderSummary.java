package io.github.giangggvu.shop.order;

import java.time.Instant;

/** One row in an order list. Line items are left out so the list loads in a single query. */
public record OrderSummary(String code, OrderStatus status, long total, Instant createdAt) {

    static OrderSummary from(Order order) {
        return new OrderSummary(order.getCode(), order.getStatus(), order.getTotal(), order.getCreatedAt());
    }
}
