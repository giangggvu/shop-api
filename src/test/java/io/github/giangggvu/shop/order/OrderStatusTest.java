package io.github.giangggvu.shop.order;

import static io.github.giangggvu.shop.order.OrderStatus.CANCELLED;
import static io.github.giangggvu.shop.order.OrderStatus.COMPLETED;
import static io.github.giangggvu.shop.order.OrderStatus.PAID;
import static io.github.giangggvu.shop.order.OrderStatus.PENDING_PAYMENT;
import static io.github.giangggvu.shop.order.OrderStatus.SHIPPING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OrderStatusTest {

    @Test
    void followsTheNormalPath() {
        assertThat(PENDING_PAYMENT.canMoveTo(PAID)).isTrue();
        assertThat(PAID.canMoveTo(SHIPPING)).isTrue();
        assertThat(SHIPPING.canMoveTo(COMPLETED)).isTrue();
    }

    @Test
    void cannotShipBeforePayment() {
        assertThat(PENDING_PAYMENT.canMoveTo(SHIPPING)).isFalse();
        assertThat(PENDING_PAYMENT.canMoveTo(COMPLETED)).isFalse();
    }

    @Test
    void cannotCancelOnceShipped() {
        assertThat(SHIPPING.canMoveTo(CANCELLED)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void finishedOrdersStayFinished(OrderStatus next) {
        assertThat(COMPLETED.canMoveTo(next)).isFalse();
        assertThat(CANCELLED.canMoveTo(next)).isFalse();
    }
}
