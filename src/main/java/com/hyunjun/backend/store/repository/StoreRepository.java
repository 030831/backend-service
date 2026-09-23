package com.hyunjun.backend.store.repository;

import com.hyunjun.backend.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);
}
