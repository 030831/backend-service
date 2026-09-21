package com.hyunjun.backend.seller.repository;

import com.hyunjun.backend.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);
}
