package com.finsentry.finsentry_ai.api;

import java.math.BigDecimal;

public record RiskIndicators(
        BigDecimal amountAnomalyMultiplier,  // e.g. 12.4 — transaction is 12.4x the customer's average
        boolean newDevice,                    // true if login used an untrusted/new device
        boolean unusualCountry,               // true if login came from outside customer's home country
        boolean rapidTransactions,            // true if 3+ transactions happened in the same time step
        boolean balanceDrained,               // true if >90% of the balance was wiped out
        int riskScore,                        // 0-100 weighted score, computed from the flags above
        RiskLevel riskLevel                   // LOW / MEDIUM / HIGH, derived from riskScore
) {
    public enum RiskLevel { LOW, MEDIUM, HIGH }
}
