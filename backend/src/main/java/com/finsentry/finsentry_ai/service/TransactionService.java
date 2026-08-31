package com.finsentry.finsentry_ai.service;


import com.finsentry.finsentry_ai.entity.TransactionInvestigationView;
import com.finsentry.finsentry_ai.repository.TransactionInvestigationViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionInvestigationViewRepository transactionRepository;

    public TransactionService(TransactionInvestigationViewRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Optional<TransactionInvestigationView> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }



}
