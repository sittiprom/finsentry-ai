package com.finsentry.finsentry_ai.tool;

import com.finsentry.finsentry_ai.entity.Customer;
import com.finsentry.finsentry_ai.service.CustomerService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CustomerTools {

    private final CustomerService customerService;

    public CustomerTools(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Tool(description = "Get customer profile by customerId including home country, account age and customer risk level ")
    public Customer retrieveCustomerProfile(@ToolParam(description = "id of customer")  String customerId){
        return customerService.getCustomerProfile(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }
}


