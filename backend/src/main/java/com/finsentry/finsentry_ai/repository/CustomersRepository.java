package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomersRepository extends JpaRepository<Customer,String> {

    @Override
    List<Customer> findAll();

    List<Customer> findByName(String customerName);

    List<Customer> findByHomeCountry(String HomeCountry);

    List<Customer> findByCustomerRiskLevel(String customerRiskLevel);
}
