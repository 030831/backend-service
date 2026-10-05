package com.hyunjun.backend.product.repository;

import com.hyunjun.backend.product.domain.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    @Query("select s from Sku s join fetch s.product where s.id in :ids")
    List<Sku> findAllWithProductByIdIn(@Param("ids") Collection<Long> ids);
}
