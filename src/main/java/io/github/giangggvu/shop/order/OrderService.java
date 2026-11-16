package io.github.giangggvu.shop.order;

import io.github.giangggvu.shop.cart.CartItemRepository;
import io.github.giangggvu.shop.cart.CartLineRef;
import io.github.giangggvu.shop.catalog.Product;
import io.github.giangggvu.shop.catalog.ProductRepository;
import io.github.giangggvu.shop.common.BusinessRuleException;
import io.github.giangggvu.shop.common.NotFoundException;
import io.github.giangggvu.shop.common.PageView;
import io.github.giangggvu.shop.common.Paging;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    /** No 0/O or 1/I, so a code read aloud or typed into a banking app cannot be confused. */
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String CODE_PREFIX = "LH";
    private static final int CODE_LENGTH = 8;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    private final SecureRandom random = new SecureRandom();
    private final OrderRepository orders;
    private final ProductRepository products;
    private final CartItemRepository cartItems;
    private final PaymentInstructions payments;

    public OrderService(
            OrderRepository orders,
            ProductRepository products,
            CartItemRepository cartItems,
            PaymentInstructions payments) {
        this.orders = orders;
        this.products = products;
        this.cartItems = cartItems;
        this.payments = payments;
    }

    /**
     * Turns the cart into an order in one transaction: lock the products, check every line, take the stock,
     * write the order, empty the cart. If any step fails, nothing changes.
     */
    @Transactional
    public OrderView checkout(Long userId, CheckoutRequest request) {
        List<CartLineRef> lines = cartItems.findLineRefs(userId);
        if (lines.isEmpty()) {
            throw new BusinessRuleException("Your cart is empty");
        }
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        lines.forEach(line -> wanted.put(line.productId(), line.quantity()));

        List<Product> locked = products.findAllByIdForUpdate(wanted.keySet());
        List<String> problems = new ArrayList<>();
        if (locked.size() != wanted.size()) {
            problems.add("Some products in your cart no longer exist");
        }
        for (Product product : locked) {
            int quantity = wanted.get(product.getId());
            if (!product.isActive()) {
                problems.add(product.getName() + " is no longer for sale");
            } else if (product.getStock() < quantity) {
                problems.add(product.getName() + ": only " + product.getStock() + " left in stock");
            }
        }
        if (!problems.isEmpty()) {
            throw new BusinessRuleException(String.join("; ", problems));
        }

        Order order = new Order(
            newOrderCode(),
            userId,
            request.recipientName().trim(),
            request.phone().trim(),
            request.address().trim(),
            blankToNull(request.note()));
        for (Product product : locked) {
            int quantity = wanted.get(product.getId());
            product.decreaseStock(quantity);
            order.addItem(product.getId(), product.getName(), product.getPrice(), quantity);
        }
        orders.save(order);
        cartItems.deleteAllForUser(userId);
        return toView(order);
    }

    @Transactional(readOnly = true)
    public PageView<OrderSummary> myOrders(Long userId, int page, int size) {
        Pageable pageable = Paging.of(page, size, NEWEST_FIRST);
        return PageView.of(orders.findByUserId(userId, pageable).map(OrderSummary::from));
    }

    @Transactional(readOnly = true)
    public OrderView myOrder(Long userId, String code) {
        return toView(findOwnedOrder(userId, code));
    }

    /** Shoppers can cancel only before paying. After that, the shop decides, because money must be refunded. */
    @Transactional
    public OrderView cancelMyOrder(Long userId, String code) {
        Order order = findOwnedOrder(userId, code);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only unpaid orders can be cancelled here. Please contact the shop.");
        }
        cancel(order);
        return toView(order);
    }

    @Transactional(readOnly = true)
    public PageView<OrderSummary> allOrders(OrderStatus status, int page, int size) {
        Pageable pageable = Paging.of(page, size, NEWEST_FIRST);
        return PageView.of((status == null ? orders.findAll(pageable) : orders.findByStatus(status, pageable))
            .map(OrderSummary::from));
    }

    @Transactional(readOnly = true)
    public OrderView anyOrder(String code) {
        return toView(findOrder(code));
    }

    @Transactional
    public OrderView changeStatus(String code, OrderStatus next) {
        Order order = findOrder(code);
        if (next == OrderStatus.CANCELLED) {
            cancel(order);
        } else {
            order.moveTo(next);
        }
        return toView(order);
    }

    private void cancel(Order order) {
        order.moveTo(OrderStatus.CANCELLED);
        Map<Long, Integer> toReturn = order.getItems().stream()
            .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity, Integer::sum));
        products.findAllByIdForUpdate(toReturn.keySet())
            .forEach(product -> product.increaseStock(toReturn.get(product.getId())));
    }

    private Order findOwnedOrder(Long userId, String code) {
        return orders.findByCodeAndUserId(code, userId)
            .orElseThrow(() -> new NotFoundException("Order " + code + " not found"));
    }

    private Order findOrder(String code) {
        return orders.findByCode(code).orElseThrow(() -> new NotFoundException("Order " + code + " not found"));
    }

    private OrderView toView(Order order) {
        return OrderView.from(order, payments.forOrder(order));
    }

    private String newOrderCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(CODE_PREFIX);
            for (int i = 0; i < CODE_LENGTH; i++) {
                builder.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = builder.toString();
        } while (orders.existsByCode(code));
        return code;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
