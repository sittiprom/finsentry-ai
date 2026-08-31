package com.finsentry.finsentry_ai.api;

import com.finsentry.finsentry_ai.entity.InvestigationCase;

import java.time.LocalDateTime;

public record InvestigationSummaryResponse(
        Long caseId,
        Long transactionId,
        InvestigationCase.CaseStatus status,
        InvestigationReport.RiskLevel riskLevel,
        Integer riskScore,
        InvestigationReport.Recommendation recommendation,
        LocalDateTime createdAt
) {}
