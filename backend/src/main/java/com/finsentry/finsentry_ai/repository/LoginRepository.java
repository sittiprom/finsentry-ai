package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginRepository extends JpaRepository<LoginHistory,Long> {


    LoginHistory findByCustomerId(Long customerId);


}
