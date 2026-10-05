package com.hyunjun.backend.order.controller;

import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.order.dto.OrderCreateRequest;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.service.OrderQueryService;
import com.hyunjun.backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody OrderCreateRequest request, @AuthenticationPrincipal LoginPrincipal principal) {
        return orderService.place(
                principal.id(),
                request,
                Instant.now()
        );
    }

    @GetMapping("/me")
    public List<OrderResponse> findMyOrders(@AuthenticationPrincipal LoginPrincipal principal) {
        return orderQueryService.findMyOrders(principal.id());
    }

    @GetMapping("/{orderId}")
    public OrderResponse getMyOrder(@PathVariable Long orderId, @AuthenticationPrincipal LoginPrincipal principal) {
        return orderQueryService.getMyOrder(orderId, principal.id());
    }
}
