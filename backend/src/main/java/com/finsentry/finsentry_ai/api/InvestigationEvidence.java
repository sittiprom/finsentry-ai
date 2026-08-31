package com.finsentry.finsentry_ai.api;

import com.finsentry.finsentry_ai.entity.Customer;
import com.finsentry.finsentry_ai.entity.LoginHistory;
import com.finsentry.finsentry_ai.entity.TransactionInvestigationView;

import java.util.List;

public record InvestigationEvidence(
        TransactionInvestigationView transaction,
        Customer customer,
        List<TransactionInvestigationView> recentTransactions,
        List<LoginHistory> recentLogins,
        RiskIndicators riskIndicators
) {}
