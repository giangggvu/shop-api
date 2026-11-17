package io.github.giangggvu.shop.cart;

import io.github.giangggvu.shop.catalog.Product;
import io.github.giangggvu.shop.catalog.ProductRepository;
import io.github.giangggvu.shop.common.BusinessRuleException;
import io.github.giangggvu.shop.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;

    public CartService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public CartView getCart(Long userId) {
        return carts.findWithProductsByUserId(userId).map(CartView::from).orElseGet(CartView::empty);
    }

    @Transactional
    public CartView addItem(Long userId, AddItemRequest request) {
        Product product = findProductForSale(request.productId());
        Cart cart = carts.findWithProductsByUserId(userId).orElseGet(() -> carts.save(new Cart(userId)));
        int alreadyInCart = cart.findItem(product.getId()).map(CartItem::getQuantity).orElse(0);
        ensureInStock(product, alreadyInCart + request.quantity());
        cart.add(product, request.quantity());
        return CartView.from(cart);
    }

    @Transactional
    public CartView updateItem(Long userId, Long productId, UpdateItemRequest request) {
        Cart cart = findCart(userId);
        CartItem item = cart.findItem(productId)
            .orElseThrow(() -> new NotFoundException("Product " + productId + " is not in your cart"));
        ensureInStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        return CartView.from(cart);
    }

    @Transactional
    public CartView removeItem(Long userId, Long productId) {
        Cart cart = findCart(userId);
        cart.remove(productId);
        return CartView.from(cart);
    }

    private Cart findCart(Long userId) {
        return carts.findWithProductsByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Your cart is empty"));
    }

    private Product findProductForSale(Long productId) {
        return products.findById(productId)
            .filter(Product::isActive)
            .orElseThrow(() -> new NotFoundException("Product " + productId + " not found"));
    }

    
    private static void ensureInStock(Product product, int quantity) {
        if (quantity > product.getStock()) {
            throw new BusinessRuleException(product.getName() + ": only " + product.getStock() + " left in stock");
        }
    }
}
