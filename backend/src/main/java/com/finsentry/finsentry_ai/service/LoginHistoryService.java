package com.finsentry.finsentry_ai.service;

import com.finsentry.finsentry_ai.entity.LoginHistory;
import com.finsentry.finsentry_ai.repository.LoginHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public List<LoginHistory> findByCustomerId(String customerId) {
        return  loginHistoryRepository.findByCustomerIdOrderByLoginTimestampDesc(customerId);
    }

}
