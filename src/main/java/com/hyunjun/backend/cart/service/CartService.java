package com.hyunjun.backend.cart.service;

import com.hyunjun.backend.cart.domain.CartItem;
import com.hyunjun.backend.cart.dto.CartItemResponse;
import com.hyunjun.backend.cart.dto.CartLineResponse;
import com.hyunjun.backend.cart.dto.CartLineRow;
import com.hyunjun.backend.cart.dto.CartOrderRequest;
import com.hyunjun.backend.cart.exception.CartItemNotFoundException;
import com.hyunjun.backend.cart.exception.CartQuantityExceededException;
import com.hyunjun.backend.cart.repository.CartItemRepository;
import com.hyunjun.backend.order.dto.OrderCreateRequest;
import com.hyunjun.backend.order.dto.OrderLineRequest;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.service.OrderQueryService;
import com.hyunjun.backend.order.service.OrderService;
import com.hyunjun.backend.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductQueryService productQueryService;
    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    @Transactional
    public CartItemResponse add(Long accountId, Long skuId, int quantity, Instant now) {
        // a. 없는 판매 단위는 404, 판매 중지 상품은 409, 주문 생성과 같은 검사를 쓴다.
        productQueryService.findOrderableSkus(List.of(skuId));

        // b. 행이 없으면 INSERT, 있으면 수량을 더한. 한 문장이라 두 번 눌러도 행은 하나다.
        cartItemRepository.addQuantity(accountId, skuId, quantity, now);

        // c.더한 결과를 읽어 상한을 검사한다. 넘으면 예외 -> b의 INSERT,UPDATE 도 롤백된다.
        CartItem cartItem = cartItemRepository.findByAccountIdAndSkuId(accountId, skuId)
                .orElseThrow(() -> new IllegalStateException("방금 저장한 장바구니 항목이 없습니다."));

        if (cartItem.getQuantity() > CartItem.MAX_QUANTITY) {
            throw new CartQuantityExceededException(
                    "장바구니에는 판매 단위마다 " + CartItem.MAX_QUANTITY + "개까지 담을 수 있습니다."
            );
        }

        return CartItemResponse.from(cartItem);
    }

    @Transactional(readOnly = true)
    public List<CartLineResponse> findMyCart(Long accountId) {
        List<CartLineRow> lines = cartItemRepository.findLines(accountId);

        return lines.stream()
                .map(CartLineResponse::from)
                .toList();
    }

    @Transactional
    public CartItemResponse changeQuantity(Long accountId, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findByIdAndAccountId(cartItemId, accountId)
                .orElseThrow(() -> new CartItemNotFoundException("장바구니 항목을 찾을 수 없습니다."));

        cartItem.changeQuantity(quantity);

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void remove(Long accountId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndAccountId(cartItemId, accountId)
                .orElseThrow(() -> new CartItemNotFoundException("장바구니 항목을 찾을 수 없습니다."));

        cartItemRepository.delete(cartItem);
    }

    /**
     * 트랜잭션을 열지 않는다. 주문 생성(OrderService.place)이 자기 트랜잭션을 커밋한 뒤에 장바구니를 지운다.
     */
    public OrderResponse order(Long accountId, CartOrderRequest request, Instant now) {
        // a. 같은 멱등키의 주문이 이미 있으면 장바구니를 읽지 않는다. 남은 고른 항목만 지우고 그 주문을 돌려준다.
        Optional<OrderResponse> existing = orderQueryService.findByIdempotencyKey(accountId, request.idempotencyKey());

        if (existing.isPresent()) {
            cartItemRepository.deleteOrdered(accountId, request.cartItemIds());
            return existing.get();
        }

        // b. 내 장바구니에서 고른 항목을 읽는다. 하나라도 없거나 남의 것이면 404.
        List<CartItem> cartItems = cartItemRepository.findAllByAccountIdAndIdIn(accountId, request.cartItemIds());

        if (cartItems.size() != Set.copyOf(request.cartItemIds()).size()) {
            throw new CartItemNotFoundException("장바구니 항목을 찾을 수 없습니다.");
        }

        // c. 장바구니 항목을 주문 생성의 입력 (skuId, 수량) 목록으로 바꾼다. 주문 쪽은 장바구니를 모른다.
        List<OrderLineRequest> lines = cartItems.stream()
                .map(cartItem -> new OrderLineRequest(cartItem.getSkuId(), cartItem.getQuantity()))
                .toList();

        OrderCreateRequest orderRequest = new OrderCreateRequest(
                request.idempotencyKey(), lines, request.delivery(), request.expectedTotal()
        );

        // d. 주문을 만든다. 실패(404,409)하면 예외가 그대로 나가고 장바구니는 그대로 남는다.
        OrderResponse order = orderService.place(accountId, orderRequest, now);

        // e. 주문이 커밋된 뒤 고른 항목을 지운다. 여기서 실패하면 주문은 남고 항목도 남는다(1.4).
        cartItemRepository.deleteOrdered(accountId, request.cartItemIds());

        return order;
    }
}
