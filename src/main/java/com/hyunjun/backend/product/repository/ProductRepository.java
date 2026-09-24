package com.hyunjun.backend.product.repository;

import com.hyunjun.backend.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);
}
