package io.github.giangggvu.shop.cart;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Reads plain numbers rather than CartItem entities. If checkout loaded the products here, it would later
     * see those copies instead of the fresh rows it locks, and could sell stock that is already gone.
     */
    @Query("""
        select new io.github.giangggvu.shop.cart.CartLineRef(i.product.id, i.quantity)
        from CartItem i
        where i.cart.userId = :userId
        order by i.product.id
        """)
    List<CartLineRef> findLineRefs(@Param("userId") Long userId);

    @Modifying
    @Query("delete from CartItem i where i.cart.id in (select c.id from Cart c where c.userId = :userId)")
    int deleteAllForUser(@Param("userId") Long userId);
}
