package com.hyunjun.backend.order.repository;

import com.hyunjun.backend.order.domain.Order;
import com.hyunjun.backend.order.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByAccountIdAndIdempotencyKey(Long accountId, String idempotencyKey);

    @Query("select o from Order o join fetch o.lines where o.id = :id and o.accountId = :accountId")
    Optional<Order> findWithLinesByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    @Query("select o from Order o join fetch o.lines where o.accountId = :accountId order by o.id desc")
    List<Order> findAllWithLinesByAccountId(@Param("accountId") Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id and o.accountId = :accountId")
    Optional<Order> findByIdAndAccountIdForUpdate(Long id, Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(Long id);

    @Query("select o.id from Order o where o.status = :status and o.expiresAt <= :now order by o.id asc")
    List<Long> findExpiredIds(OrderStatus status, Instant now, Pageable pageable);
}
