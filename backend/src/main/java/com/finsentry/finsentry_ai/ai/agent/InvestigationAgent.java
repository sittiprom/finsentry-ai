package com.finsentry.finsentry_ai.ai.agent;


import com.finsentry.finsentry_ai.api.InvestigationReport;
import com.finsentry.finsentry_ai.tool.CustomerTools;
import com.finsentry.finsentry_ai.tool.LoginTool;
import com.finsentry.finsentry_ai.tool.RiskTools;
import com.finsentry.finsentry_ai.tool.TransactionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class InvestigationAgent {

    private final ChatClient chatClient;
    private final TransactionTools transactionTools;
    private final CustomerTools customerTools;
    private final LoginTool loginTool;
    private final RiskTools riskTools;


    public InvestigationAgent(ChatClient chatClient, TransactionTools transactionTools, CustomerTools customerTools, LoginTool loginTool, RiskTools riskTools) {
        this.chatClient = chatClient;
        this.transactionTools = transactionTools;
        this.customerTools = customerTools;
        this.loginTool = loginTool;
        this.riskTools = riskTools;
    }

    public InvestigationReport investigate(Long transactionId) {
        return chatClient.prompt()
                .user("Investigate transaction " + transactionId)
                .tools(transactionTools, customerTools, loginTool, riskTools)
                .call()
                .entity(InvestigationReport.class);

    }
}
