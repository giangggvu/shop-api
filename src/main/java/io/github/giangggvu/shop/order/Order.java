package io.github.giangggvu.shop.order;

import io.github.giangggvu.shop.common.BusinessRuleException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity(name = "ShopOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short public code. The shopper types it as the bank transfer note, which is how a payment finds its order. */
    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private long total;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(String code, Long userId, String recipientName, String phone, String address, String note) {
        this.code = code;
        this.userId = userId;
        this.recipientName = recipientName;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.total = 0;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    /** Copies name and price into the order, so later price changes never rewrite what was bought. */
    public void addItem(Long productId, String productName, long unitPrice, int quantity) {
        items.add(new OrderItem(this, productId, productName, unitPrice, quantity));
        total += unitPrice * quantity;
    }

    public void moveTo(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new BusinessRuleException("Order " + code + " cannot go from " + status + " to " + next);
        }
        status = next;
        if (next == OrderStatus.PAID) {
            paidAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getTotal() {
        return total;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
