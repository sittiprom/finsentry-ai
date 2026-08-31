package com.finsentry.finsentry_ai.controller;

import com.finsentry.finsentry_ai.service.InvestigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    public TransactionController(InvestigationService investigationService) {}
}
