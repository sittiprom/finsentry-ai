package com.finsentry.finsentry_ai.service;

import com.finsentry.finsentry_ai.ai.agent.InvestigationAgent;

import com.finsentry.finsentry_ai.ai.validator.ReportValidator;
import com.finsentry.finsentry_ai.api.InvestigationResponse;
import com.finsentry.finsentry_ai.api.InvestigationSummaryResponse;
import com.finsentry.finsentry_ai.entity.InvestigationCase;
import com.finsentry.finsentry_ai.entity.InvestigationReportEntity;
import com.finsentry.finsentry_ai.entity.TransactionInvestigationView;
import com.finsentry.finsentry_ai.repository.InvestigationCaseRepository;
import com.finsentry.finsentry_ai.repository.InvestigationReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvestigationService {

    private final TransactionService transactionService;
    private final InvestigationCaseRepository caseRepository;
    private final InvestigationReportRepository reportRepository;
    private final InvestigationAgent investigationAgent;
    private final ReportValidator reportValidator;

    private static final Logger log = LoggerFactory.getLogger(InvestigationService.class);

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelUsed;
    public InvestigationService(TransactionService transactionService,
                                InvestigationCaseRepository caseRepository, InvestigationReportRepository reportRepository, InvestigationAgent investigationAgent, ReportValidator reportValidator) {
        this.transactionService = transactionService;
        this.caseRepository = caseRepository;
        this.reportRepository = reportRepository;
        this.investigationAgent = investigationAgent;
        this.reportValidator = reportValidator;
    }



    public InvestigationResponse investigate(Long transactionId) {

        // 1. Make sure transaction exists
        TransactionInvestigationView transaction =
                transactionService.getTransactionById(transactionId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Transaction not found: " + transactionId
                                )
                        );


        // 2. Create case
        var investigationCase = new InvestigationCase();
        investigationCase.setTransactionId(transactionId);
        investigationCase.setStatus(InvestigationCase.CaseStatus.PENDING);

        investigationCase = caseRepository.save(investigationCase);

        try {

            investigationCase.setStatus(InvestigationCase.CaseStatus.IN_PROGRESS);
            caseRepository.save(investigationCase);

            long start = System.currentTimeMillis();
            var aiResult = investigationAgent.investigate(
                    transaction.getId()
            );

            var validation = reportValidator.validate(aiResult);

            if (!validation.valid()){
                log.warn("Investigation case {} report failed validation: {}",
                        investigationCase.getId(), validation.issues());
                aiResult = reportValidator.applySafeDefaults(aiResult);
            }

            // 6. Persist AI report
            var report = new InvestigationReportEntity();

            int executionTimeMs = (int) (System.currentTimeMillis() - start);
            report.setCaseId(investigationCase.getId());
            report.setRiskLevel(aiResult.riskLevel());
            report.setRiskScore(aiResult.riskScore());
            report.setSummary(aiResult.summary());
            report.setFindings(aiResult.findings());
            report.setPolicyMatches(aiResult.policyMatches());
            report.setRecommendation(aiResult.recommendation());
            report.setExecutionTimeMs(executionTimeMs);
            report.setModelUsed(modelUsed);

            reportRepository.save(report);

            // 7. Done
            investigationCase.setStatus(InvestigationCase.CaseStatus.COMPLETED);
            investigationCase.setCompletedAt(LocalDateTime.now());
            caseRepository.save(investigationCase);

            return new InvestigationResponse(
                    investigationCase.getId(),
                    transactionId,
                    investigationCase.getStatus(),
                    aiResult.riskLevel(),
                    aiResult.riskScore(),
                    aiResult.summary(),
                    aiResult.findings(),
                    aiResult.policyMatches(),
                    aiResult.recommendation()
            );

        } catch (Exception ex) {
            investigationCase.setStatus(InvestigationCase.CaseStatus.FAILED);
            caseRepository.save(investigationCase);

            throw ex;
        }
    }

    public List<InvestigationSummaryResponse> listInvestigations(int limit) {
        return caseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .limit(limit)
                .map(this::toSummary)
                .toList();
    }

    private InvestigationSummaryResponse toSummary(InvestigationCase c) {
        var report = reportRepository.findByCaseId(c.getId()).orElse(null);
        return new InvestigationSummaryResponse(

                c.getId(),
                c.getTransactionId(),
                c.getStatus(),
                report != null ? report.getRiskLevel() : null,
                report != null ? report.getRiskScore() : null,
                report != null ? report.getRecommendation() : null,
                c.getCreatedAt()
        );
    }

    public InvestigationResponse getInvestigationReportByCaseId(Long caseId) {
        var investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found: " + caseId));


        InvestigationReportEntity report = reportRepository.findByCaseId(caseId).orElseThrow(
                () ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No case Not found: " + caseId
                        ));


        return new InvestigationResponse(
                caseId,
                investigationCase.getTransactionId(),
                investigationCase.getStatus(),
                report != null ? report.getRiskLevel() : null,
                report != null ? report.getRiskScore() : null,
                report != null ? report.getSummary() : null,
                report != null ? report.getFindings() : null,
                report != null ? report.getPolicyMatches() : null,
                report != null ? report.getRecommendation() : null
        );


    }


}