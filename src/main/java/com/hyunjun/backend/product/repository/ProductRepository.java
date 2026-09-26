package com.hyunjun.backend.product.repository;

import com.hyunjun.backend.product.domain.Product;
import com.hyunjun.backend.product.domain.ProductStatus;
import com.hyunjun.backend.product.dto.ProductSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    @Query(value = """
            select new com.hyunjun.backend.product.dto.ProductSummaryRow(
                    p.id, p.name, min(s.price), sum(st.quantity))
            from Product p
            join p.skus s
            join Stock st on st.skuId = s.id
            where p.status = :status
            group by p.id, p.name
            order by p.id desc
            """,
            countQuery = "select count(p) from Product p where p.status = :status")
    Page<ProductSummaryRow> findSummaries(@Param("status") ProductStatus status, Pageable pageable);

    @Query("select p from Product p join fetch p.skus where p.id = :id and p.status = :status")
    Optional<Product> findWithSkusByIdAndStatus(@Param("id") Long id,
                                                @Param("status") ProductStatus status);
}
