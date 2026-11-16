package io.github.giangggvu.shop.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /** The cart with its items and their products, in one query, ready to show. */
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findWithProductsByUserId(Long userId);
}
