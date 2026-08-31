package com.finsentry.finsentry_ai.entity;

import com.finsentry.finsentry_ai.api.InvestigationReport;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "investigation_reports")
public class InvestigationReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false, unique = true)
    private Long caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private InvestigationReport.RiskLevel riskLevel;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> findings;

    @Type(JsonType.class)
    @Column(name = "policy_matches", columnDefinition = "jsonb", nullable = false)
    private List<String> policyMatches;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestigationReport.Recommendation recommendation;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();



}