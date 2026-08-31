package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByCustomerIdOrderByLoginTimestampDesc(String customerId);

    List<LoginHistory>  findTop10ByCustomerIdOrderByLoginTimestampDesc(String customerId);

    Optional<LoginHistory> findByTransactionId(Long transactionId);
}
