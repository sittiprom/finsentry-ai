package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
