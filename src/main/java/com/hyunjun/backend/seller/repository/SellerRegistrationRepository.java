package com.hyunjun.backend.seller.repository;

import com.hyunjun.backend.seller.domain.RegistrationStatus;
import com.hyunjun.backend.seller.domain.SellerRegistration;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellerRegistrationRepository extends JpaRepository<SellerRegistration, Long> {

    boolean existsByApplicant_IdAndStatus(
            Long accountId,
            RegistrationStatus status
    );

    Optional<SellerRegistration> findByIdAndApplicant_Id(
            Long id,
            Long accountId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from SellerRegistration r where r.id = :id")
    Optional<SellerRegistration> findForUpdate(@Param("id") Long id);

}
