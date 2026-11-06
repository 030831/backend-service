package com.hyunjun.backend.cart.repository;


import com.hyunjun.backend.cart.domain.CartItem;
import com.hyunjun.backend.cart.dto.CartLineRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Modifying
    @Query(nativeQuery = true, value = """
                INSERT INTO cart_items (account_id, sku_id, quantity, created_at, updated_at)
                VALUES (:accountId, :skuId, :quantity, :now, :now)
                ON DUPLICATE KEY UPDATE
                    quantity = quantity + :quantity,
                    updated_at = :now
            """)
    void addQuantity(Long accountId, Long skuId, int quantity, Instant now);

    Optional<CartItem> findByAccountIdAndSkuId(Long accountId, Long skuId);

    Optional<CartItem> findByIdAndAccountId(Long id, Long accountId);

    @Query("""
        select new com.hyunjun.backend.cart.dto.CartLineRow(c.id, s.id, p.name, s.optionLabel, 
         s.price, c.quantity, st.quantity, p.status)
        from CartItem  c
        join Sku s on s.id = c.skuId
        join s.product p
        join Stock st on st.skuId = s.id
        where c.accountId = :accountId
        order by c.id desc
    """)
    List<CartLineRow> findLines(Long accountId);

    List<CartItem> findAllByAccountIdAndIdIn(Long accountId, Collection<Long> ids);

    @Transactional
    @Modifying
    @Query("delete from CartItem c where c.accountId = :accountId and c.id in :ids")
    int deleteOrdered(Long accountId, Collection<Long> ids);
}
