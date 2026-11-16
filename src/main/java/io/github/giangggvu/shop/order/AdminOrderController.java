package io.github.giangggvu.shop.order;

import io.github.giangggvu.shop.common.PageView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@SecurityRequirement(name = "bearer")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageView<OrderSummary> list(
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return orderService.allOrders(status, page, size);
    }

    @GetMapping("/{code}")
    public OrderView get(@PathVariable("code") String code) {
        return orderService.anyOrder(code);
    }

    /** For example, mark an order PAID once the transfer shows up in the bank account. */
    @PatchMapping("/{code}/status")
    public OrderView changeStatus(@PathVariable("code") String code, @Valid @RequestBody StatusChangeRequest request) {
        return orderService.changeStatus(code, request.status());
    }
}
