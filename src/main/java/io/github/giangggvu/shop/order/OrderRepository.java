package io.github.giangggvu.shop.order;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByCode(String code);

    /** Looks up by owner too, so one customer can never open another customer's order. */
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByCodeAndUserId(String code, Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
