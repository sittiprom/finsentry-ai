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

@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "device_type")
    private String device_type;

    @Column(name = "first_seen_at")
    private LocalDateTime first_seen_at = LocalDateTime.now();

    @Column(name = "trusted",nullable = false)
    private boolean trusted;

}
