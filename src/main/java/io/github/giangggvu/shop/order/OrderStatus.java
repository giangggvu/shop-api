package io.github.giangggvu.shop.order;

/**
 * The life of an order. Each status lists where it may go next, so an order cannot, for example,
 * be shipped before it is paid, or brought back to life after it is cancelled.
 */
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
