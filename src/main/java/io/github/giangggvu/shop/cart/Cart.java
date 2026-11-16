package io.github.giangggvu.shop.cart;

import io.github.giangggvu.shop.catalog.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** One cart per user, kept in the database so it survives logging in on another device. */
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Cart() {
    }

    public Cart(Long userId) {
        this.userId = userId;
        touch();
    }

    public Optional<CartItem> findItem(Long productId) {
        return items.stream().filter(item -> item.getProduct().getId().equals(productId)).findFirst();
    }

    public void add(Product product, int quantity) {
        findItem(product.getId()).ifPresentOrElse(
            item -> item.setQuantity(item.getQuantity() + quantity),
            () -> items.add(new CartItem(this, product, quantity)));
        touch();
    }

    public void remove(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
        touch();
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
