package com.finsentry.finsentry_ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private String customerId;

    private String name;

    @Column(name = "home_country")
    private String homeCountry;

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt = LocalDateTime.now();

    @Column(name = "customer_risk_level")
    private String customerRiskLevel;

    @Column(name = "average_monthly_transactions")
    private BigDecimal averageMonthlyTransactions;

}
