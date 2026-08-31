package com.finsentry.finsentry_ai.ai.validator;

import com.finsentry.finsentry_ai.api.InvestigationReport;
import com.finsentry.finsentry_ai.api.InvestigationReport.Recommendation;
import com.finsentry.finsentry_ai.api.InvestigationReport.RiskLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ReportValidator {

    // Must match the policy IDs actually defined in fraud_investigation_policy.pdf
    private static final Set<String> KNOWN_POLICY_IDS = Set.of(
            "POLICY-TRANSACTION-001", "POLICY-TRANSACTION-002", "POLICY-TRANSACTION-003",
            "POLICY-TRANSACTION-004", "POLICY-TRANSACTION-005",
            "POLICY-DEVICE-001", "POLICY-DEVICE-002", "POLICY-DEVICE-003",
            "POLICY-MERCHANT-001", "POLICY-MERCHANT-002"
    );

    public ValidationResult validate(InvestigationReport report) {
        List<String> issues = new ArrayList<>();

        // 1. Risk score must fall in the expected 0-100 range
        if (report.riskScore() < 0 || report.riskScore() > 100) {
            issues.add("Risk score out of range: " + report.riskScore());
        }

        // 2. Risk level and recommendation must be logically consistent
        if (report.riskLevel() == RiskLevel.HIGH && report.recommendation() == Recommendation.NO_ACTION) {
            issues.add("HIGH risk level paired with NO_ACTION recommendation");
        }
        if (report.riskLevel() == RiskLevel.LOW && report.recommendation() == Recommendation.ESCALATE_FOR_MANUAL_REVIEW) {
            issues.add("LOW risk level paired with ESCALATE_FOR_MANUAL_REVIEW — inconsistent");
        }

        if (report.riskLevel() == RiskLevel.LOW && report.recommendation() != Recommendation.NO_ACTION) {
            issues.add("LOW risk level should recommend NO_ACTION per policy risk table");
        }

        // 3. HIGH risk requires supporting evidence (spec Section 21 — explainability requirement)
        if (report.riskLevel() == RiskLevel.HIGH && (report.findings() == null || report.findings().isEmpty())) {
            issues.add("HIGH risk level with no findings — explainability requirement violated");
        }

        if (report.riskLevel() == RiskLevel.MEDIUM && report.recommendation() == Recommendation.ESCALATE_FOR_MANUAL_REVIEW) {
            issues.add("MEDIUM risk level should recommend REVIEW, not ESCALATE, per policy risk table");
        }

        // 4. Every cited policy must actually exist — catches hallucinated citations
        if (report.policyMatches() != null) {
            for (String policyId : report.policyMatches()) {
                if (!KNOWN_POLICY_IDS.contains(policyId)) {
                    issues.add("Unknown policy cited (possible hallucination): " + policyId);
                }
            }
        }

        if (report.riskLevel() == RiskLevel.HIGH && report.riskScore() < 70) {
            issues.add("HIGH risk level doesn't match risk score of " + report.riskScore());
        }
        if (report.riskLevel() == RiskLevel.MEDIUM && (report.riskScore() < 35 || report.riskScore() >= 70)) {
            issues.add("MEDIUM risk level doesn't match risk score of " + report.riskScore());
        }
        if (report.riskLevel() == RiskLevel.LOW && report.riskScore() >= 35) {
            issues.add("LOW risk level doesn't match risk score of " + report.riskScore());
        }

        if (report.recommendation() == null) {
            issues.add("Missing recommendation");
        }

        return new ValidationResult(issues.isEmpty(), issues);
    }


    public InvestigationReport applySafeDefaults(InvestigationReport report) {
        InvestigationReport corrected = report;

        if (corrected.recommendation() == Recommendation.ESCALATE_FOR_MANUAL_REVIEW
                && (corrected.findings() == null || corrected.findings().isEmpty())) {
            corrected = withRecommendation(corrected, Recommendation.REVIEW, "[Downgraded: no supporting evidence]");
        }

        RiskLevel correctLevel = corrected.riskScore() >= 70 ? RiskLevel.HIGH
                : corrected.riskScore() >= 35 ? RiskLevel.MEDIUM
                  : RiskLevel.LOW;

        if (corrected.riskLevel() != correctLevel) {
            corrected = withRiskLevel(corrected, correctLevel,
                    "[Corrected: risk level adjusted from " + corrected.riskLevel() + " to " + correctLevel +
                            " to match the computed risk score of " + corrected.riskScore() + "]");
        }

        if (corrected.riskLevel() == RiskLevel.LOW && corrected.recommendation() != Recommendation.NO_ACTION) {
            corrected = withRecommendation(corrected, Recommendation.NO_ACTION, "[Corrected: LOW risk should be NO_ACTION per policy]");
        }
        if (corrected.riskLevel() == RiskLevel.MEDIUM && corrected.recommendation() == Recommendation.ESCALATE_FOR_MANUAL_REVIEW) {
            corrected = withRecommendation(corrected, Recommendation.REVIEW, "[Corrected: MEDIUM risk should recommend REVIEW per policy]");
        }

        return corrected;
    }

    private InvestigationReport withRiskLevel(InvestigationReport report, RiskLevel level, String note) {
        return new InvestigationReport(
                level, report.riskScore(), report.summary() + " " + note,
                report.findings(), report.policyMatches(), report.recommendation()
        );
    }

    private InvestigationReport withRecommendation(InvestigationReport report, Recommendation rec, String note) {
        return new InvestigationReport(
                report.riskLevel(), report.riskScore(), report.summary() + " " + note,
                report.findings(), report.policyMatches(), rec
        );
    }

    public record ValidationResult(boolean valid, List<String> issues) {}
}