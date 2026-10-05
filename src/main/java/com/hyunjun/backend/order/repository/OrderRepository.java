package com.hyunjun.backend.order.repository;

import com.hyunjun.backend.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByAccountIdAndIdempotencyKey(Long accountId, String idempotencyKey);

    @Query("select o from Order o join fetch o.lines where o.id = :id and o.accountId = :accountId")
    Optional<Order> findWithLinesByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    @Query("select o from Order o join fetch o.lines where o.accountId = :accountId order by o.id desc")
    List<Order> findAllWithLinesByAccountId(@Param("accountId") Long accountId);
}
