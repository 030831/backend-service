package com.hyunjun.backend.admin.repository;

import com.hyunjun.backend.admin.domain.AdminAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {
    Optional<AdminAccount> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdminAccount a where a.id = :id")
    Optional<AdminAccount> findByIdForUpdate(@Param("id") Long id);
}
