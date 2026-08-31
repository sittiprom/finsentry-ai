package com.finsentry.finsentry_ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "login_history")
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_id")
    private Long loginId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "login_timestamp")
    private LocalDateTime loginTimestamp = LocalDateTime.now();

    private  String country;

    @Column(name = "ip_address")
    private  String ipAddress;

    private Boolean successful;

    @Column(name = "transaction_id")
    private Long transactionId;

}
