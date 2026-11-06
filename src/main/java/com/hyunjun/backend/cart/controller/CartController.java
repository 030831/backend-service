package com.hyunjun.backend.cart.controller;

import com.hyunjun.backend.cart.dto.CartAddRequest;
import com.hyunjun.backend.cart.dto.CartItemResponse;
import com.hyunjun.backend.cart.dto.CartLineResponse;
import com.hyunjun.backend.cart.dto.CartQuantityRequest;
import com.hyunjun.backend.cart.service.CartService;
import com.hyunjun.backend.common.security.LoginPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public CartItemResponse add(@Valid @RequestBody CartAddRequest request, @AuthenticationPrincipal LoginPrincipal principal) {
        return cartService.add(principal.id(), request.skuId(), request.quantity(), Instant.now());
    }

    @GetMapping
    public List<CartLineResponse> findMyCart(@AuthenticationPrincipal LoginPrincipal principal) {
        return cartService.findMyCart(principal.id());
    }

    @PatchMapping("/items/{cartItemId}")
    public CartItemResponse changeQuantity(@PathVariable Long cartItemId, @Valid @RequestBody CartQuantityRequest request,
                                           @AuthenticationPrincipal LoginPrincipal principal) {
        return cartService.changeQuantity(principal.id(), cartItemId, request.quantity());
    }

    @DeleteMapping("/items/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void remove(@PathVariable Long cartItemId, @AuthenticationPrincipal LoginPrincipal principal) {
        cartService.remove(principal.id(), cartItemId);
    }
}

