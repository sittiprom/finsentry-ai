package com.finsentry.finsentry_ai.api;



import java.util.List;


public record InvestigationReport(
        RiskLevel riskLevel,
        Integer riskScore,
        String summary,
        List<String> findings,
        List<String> policyMatches,
        Recommendation recommendation
) {
    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }

    public enum Recommendation {
        NO_ACTION, REVIEW, ESCALATE_FOR_MANUAL_REVIEW
    }
}