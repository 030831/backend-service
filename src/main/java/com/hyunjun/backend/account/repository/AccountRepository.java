package com.hyunjun.backend.account.repository;

import com.hyunjun.backend.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Account> findByEmail(String email);
}
