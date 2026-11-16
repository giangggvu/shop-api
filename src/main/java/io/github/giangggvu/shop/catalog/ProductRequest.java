package io.github.giangggvu.shop.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotNull Long categoryId,
    @NotBlank @Size(max = 200) String name,
    @Size(max = 2000) String description,
    @NotNull @PositiveOrZero Long price,
    @NotNull @PositiveOrZero Integer stock,
    @Size(max = 500) String imageUrl,
    Boolean active) {

    /** A product is for sale unless the request says otherwise. */
    public boolean activeOrDefault() {
        return active == null || active;
    }
}
