package com.finsentry.finsentry_ai.service;

import com.finsentry.finsentry_ai.entity.Customer;
import com.finsentry.finsentry_ai.entity.TransactionInvestigationView;
import com.finsentry.finsentry_ai.repository.CustomersRepository;
import com.finsentry.finsentry_ai.repository.TransactionInvestigationViewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomersRepository customerRepository;
    private final TransactionInvestigationViewRepository transactionRepository;

    public CustomerService(CustomersRepository customerRepository,
                           TransactionInvestigationViewRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }


    public Optional<Customer> getCustomerProfile(String id) {
        return customerRepository.findById(id);
    }

    public List<Customer> findByCustomerName(String customerName) {
        return customerRepository.findByName(customerName);
    }

    public List<TransactionInvestigationView> getRecentTransactions(String customerId, int limit) {
        return transactionRepository.findByNameOrigOrderByStepDesc(customerId)
                .stream()
                .limit(limit)
                .toList();
    }
}
