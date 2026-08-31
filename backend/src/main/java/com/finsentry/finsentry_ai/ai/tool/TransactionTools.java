package com.finsentry.finsentry_ai.ai.tool;

import com.finsentry.finsentry_ai.entity.TransactionInvestigationView;
import com.finsentry.finsentry_ai.service.CustomerService;
import com.finsentry.finsentry_ai.service.TransactionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TransactionTools {

    private final TransactionService transactionService;
    private final CustomerService customerService;

    public TransactionTools(TransactionService transactionService, CustomerService customerService) {
        this.transactionService = transactionService;
        this.customerService = customerService;
    }

    @Tool(description = "Get transaction details by ID, including amount, type, origin and destination accounts, and balances")
    public TransactionInvestigationView getTransaction(Long id ){
        return transactionService.getTransactionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

    }

    @Tool(description = "Get list of transactions by customerID and limit of transaction such as 10 transactions" +
            " including amount, type, origin and destination accounts, and balances")
    public List<TransactionInvestigationView> getRecentTransactions(@ToolParam(description = "id of customer" +
            "and number of transaction") String customerId, int limit){
        return customerService.getRecentTransactions(customerId, limit);

    }

}
