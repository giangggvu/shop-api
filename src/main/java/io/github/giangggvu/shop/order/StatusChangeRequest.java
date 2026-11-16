package io.github.giangggvu.shop.order;

import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(@NotNull OrderStatus status) {
}
