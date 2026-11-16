package io.github.giangggvu.shop.order;

import io.github.giangggvu.shop.common.CurrentUser;
import io.github.giangggvu.shop.common.PageView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearer")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Places an order from everything in the cart. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView checkout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(CurrentUser.id(jwt), request);
    }

    @GetMapping
    public PageView<OrderSummary> myOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return orderService.myOrders(CurrentUser.id(jwt), page, size);
    }

    @GetMapping("/{code}")
    public OrderView myOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable("code") String code) {
        return orderService.myOrder(CurrentUser.id(jwt), code);
    }

    @PostMapping("/{code}/cancel")
    public OrderView cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable("code") String code) {
        return orderService.cancelMyOrder(CurrentUser.id(jwt), code);
    }
}
