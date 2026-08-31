package com.finsentry.finsentry_ai.controller;

import com.finsentry.finsentry_ai.api.CopilotRequest;
import com.finsentry.finsentry_ai.api.CopilotResponse;
import com.finsentry.finsentry_ai.service.InvestigationService;
import com.finsentry.finsentry_ai.tool.CustomerTools;
import com.finsentry.finsentry_ai.tool.LoginTool;
import com.finsentry.finsentry_ai.tool.RiskTools;
import com.finsentry.finsentry_ai.tool.TransactionTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CopilotController {

    private static final String SYSTEM_PROMPT = """
    You are the FinSentry AI investigator copilot. You help with:
    - Transaction details and history
    - Customer profiles and identity (name, country, risk level, account info)
    - Fraud risk indicators and scoring
    - Fraud and compliance policy questions
    - Since staff may not know country abbreviation. Please tell it in full name.

    Only decline questions that are clearly unrelated to fraud investigation.
    If a question is about a specific case, customer, or transaction, always
    attempt to answer it using the available tools before declining.
    """;

    private final ChatClient chatClient;
    private final TransactionTools transactionTools;
    private final CustomerTools customerTools;
    private final LoginTool loginTool;
    private final RiskTools riskTools;
    private final InvestigationService investigationService;

    public CopilotController(ChatClient chatClient, TransactionTools transactionTools, CustomerTools customerTools, LoginTool loginTool, RiskTools riskTools, InvestigationService investigationService) {
        this.chatClient = chatClient;
        this.transactionTools = transactionTools;
        this.customerTools = customerTools;
        this.loginTool = loginTool;
        this.riskTools = riskTools;
        this.investigationService = investigationService;
    }

    @PostMapping("/api/copilot/ask")
    public CopilotResponse ask(@RequestBody CopilotRequest request) {
        String contextPrefix = "";

        if (request.getCaseId() != null) {
            var investigationCase = investigationService.getInvestigationReportByCaseId(request.getCaseId());

            if (investigationCase != null) {
                contextPrefix = "The user is asking about investigation case #" + request.getCaseId() +
                        ", which concerns transaction ID " + investigationCase.transactionId() +
                        ". When the question refers to \"this case\" or \"this transaction\", " +
                        "call getTransaction with transaction ID " + investigationCase.transactionId() +
                        " directly. ";
            }
        }

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(contextPrefix + request.getQuestion())
                .tools(transactionTools, customerTools, loginTool, riskTools)
                .call()
                .entity(CopilotResponse.class);
    }
}
