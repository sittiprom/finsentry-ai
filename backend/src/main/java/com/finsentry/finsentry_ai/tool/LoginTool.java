package com.finsentry.finsentry_ai.tool;

import com.finsentry.finsentry_ai.entity.Customer;
import com.finsentry.finsentry_ai.entity.LoginHistory;
import com.finsentry.finsentry_ai.repository.LoginRepository;
import com.finsentry.finsentry_ai.service.LoginHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginTool {

    private static final Logger log = LoggerFactory.getLogger(LoginTool.class);
    private final LoginHistoryService loginHistoryService;

    public LoginTool(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @Tool(description = "Getting login history by customer Id. This method provide the detail of login including " +
            " timestamp, country during login, device id , ip address, transaction id and loging status ( success or not)")
    public List<LoginHistory> getRecentLoginHistory(@ToolParam(description = "id of customer") String customerId)
    {
         return loginHistoryService.findByCustomerId(customerId);
    }
}
