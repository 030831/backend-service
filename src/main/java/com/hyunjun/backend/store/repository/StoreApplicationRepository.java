package com.hyunjun.backend.store.repository;

import com.hyunjun.backend.store.domain.ApplicationStatus;
import com.hyunjun.backend.store.domain.StoreApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreApplicationRepository extends JpaRepository<StoreApplication, Long> {
    List<StoreApplication> findAllByApplicantAccountIdOrderByIdDesc(Long applicantAccountId);

    List<StoreApplication> findAllByStatusOrderByIdAsc(ApplicationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from StoreApplication a where a.id = :id")
    Optional<StoreApplication> findByIdForUpdate(@Param("id") Long id);
}
