package com.hyunjun.backend.product.repository;

import com.hyunjun.backend.product.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySkuId(Long skuId);

    List<Stock> findAllBySkuIdIn(List<Long> skuIds);

    @Modifying
    @Query("""
            update Stock s
            set s.quantity = s.quantity + :delta
            where s.skuId = :skuId
            and s.quantity + :delta >= 0
    """)
    int adjustQuantity(@Param("skuId") Long skuId, @Param("delta") int delta);
}

