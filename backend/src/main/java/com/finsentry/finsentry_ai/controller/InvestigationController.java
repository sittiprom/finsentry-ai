package com.finsentry.finsentry_ai.controller;

import com.finsentry.finsentry_ai.api.InvestigationRequest;


import com.finsentry.finsentry_ai.api.InvestigationResponse;
import com.finsentry.finsentry_ai.api.InvestigationSummaryResponse;
import com.finsentry.finsentry_ai.entity.InvestigationReportEntity;
import com.finsentry.finsentry_ai.service.InvestigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InvestigationController {

    private static final Logger log = LoggerFactory.getLogger(InvestigationController.class);
    private final InvestigationService investigationService;


    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping("/api/investigations")
    public ResponseEntity<InvestigationResponse> startInvestigation(@RequestBody InvestigationRequest investigationRequest) {
        log.info("Starting investigation for transactionId {}", investigationRequest.getTransactionId());
        InvestigationResponse response = investigationService.investigate(investigationRequest.getTransactionId());
        return ResponseEntity.ok(response);
    }

   @GetMapping("/api/investigations")
    public List<InvestigationSummaryResponse> listInvestigations(@RequestParam(defaultValue = "100") int limit) {
        log.info("Getting investigations");
       return investigationService.listInvestigations(limit);

   }

    @GetMapping("/api/investigations/{caseId}")
    public InvestigationResponse getInvestigateCase(@PathVariable Long caseId) {
        log.info("Getting investigate case for transactionId {}", caseId);
        return investigationService.getInvestigationReportByCaseId(caseId);
    }
}
