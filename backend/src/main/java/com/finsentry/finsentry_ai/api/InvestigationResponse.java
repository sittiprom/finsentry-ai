package com.finsentry.finsentry_ai.api;


import com.finsentry.finsentry_ai.entity.InvestigationCase;

import java.util.List;

public record InvestigationResponse(
        Long caseId,
        Long transactionId,
        InvestigationCase.CaseStatus status,
        InvestigationReport.RiskLevel riskLevel,
        Integer riskScore,
        String summary,
        List<String> findings,
        List<String> policyMatches,
        InvestigationReport.Recommendation recommendation
) {}
