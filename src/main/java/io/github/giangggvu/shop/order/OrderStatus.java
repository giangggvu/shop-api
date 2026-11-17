package io.github.giangggvu.shop.order;


public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPING,
    COMPLETED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == PAID || next == CANCELLED;
            case PAID -> next == SHIPPING || next == CANCELLED;
            case SHIPPING -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
