package com.hyunjun.backend.cart.service;

import com.hyunjun.backend.cart.domain.CartItem;
import com.hyunjun.backend.cart.dto.CartItemResponse;
import com.hyunjun.backend.cart.dto.CartLineResponse;
import com.hyunjun.backend.cart.dto.CartLineRow;
import com.hyunjun.backend.cart.exception.CartItemNotFoundException;
import com.hyunjun.backend.cart.exception.CartQuantityExceededException;
import com.hyunjun.backend.cart.repository.CartItemRepository;
import com.hyunjun.backend.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductQueryService productQueryService;

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
}
