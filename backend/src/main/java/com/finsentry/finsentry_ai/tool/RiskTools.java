package com.finsentry.finsentry_ai.tool;

import com.finsentry.finsentry_ai.api.RiskIndicators;
import com.finsentry.finsentry_ai.service.CustomerService;
import com.finsentry.finsentry_ai.service.RiskService;
import com.finsentry.finsentry_ai.service.TransactionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RiskTools {

    private final RiskService riskService;
    private final TransactionService transactionService;
    private final CustomerService customerService;

    public RiskTools(RiskService riskService, TransactionService transactionService, CustomerService customerService) {
        this.riskService = riskService;
        this.transactionService = transactionService;
        this.customerService = customerService;
    }

    @Tool(description = "Calculate deterministic risk indicators for a transaction")
    public RiskIndicators calculateRiskIndicators(Long transactionId) {
        var transaction = transactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        var customer = customerService.getCustomerProfile(transaction.getNameOrig());
        if (customer.isEmpty()) {
            // no synthetic customer/login data exists for this transaction —
            // return zeroed-out indicators rather than throwing, so the LLM
            // gets an honest "nothing computed" signal instead of guessing
            return new RiskIndicators(BigDecimal.ZERO, false, false, false, false, 0, RiskIndicators.RiskLevel.LOW);
        }
        return riskService.calculateRiskIndicators(transaction, customer.get());
    }
}
