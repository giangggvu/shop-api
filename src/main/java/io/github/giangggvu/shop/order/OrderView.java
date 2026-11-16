package io.github.giangggvu.shop.order;

import java.time.Instant;
import java.util.List;

public record OrderView(
    String code,
    OrderStatus status,
    long total,
    String recipientName,
    String phone,
    String address,
    String note,
    Instant createdAt,
    Instant paidAt,
    List<Line> items,
    PaymentView payment) {

    public record Line(Long productId, String productName, long unitPrice, int quantity, long lineTotal) {

        static Line from(OrderItem item) {
            return new Line(
                item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
        }
    }

    static OrderView from(Order order, PaymentView payment) {
        return new OrderView(
            order.getCode(),
            order.getStatus(),
            order.getTotal(),
            order.getRecipientName(),
            order.getPhone(),
            order.getAddress(),
            order.getNote(),
            order.getCreatedAt(),
            order.getPaidAt(),
            order.getItems().stream().map(Line::from).toList(),
            payment);
    }
}
