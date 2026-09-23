package com.hyunjun.backend.store.repository;

import com.hyunjun.backend.store.domain.StoreApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreApplicationRepository extends JpaRepository<StoreApplication, Long> {
    List<StoreApplication> findAllByApplicantAccountIdOrderByIdDesc(Long applicantAccountId);
}
