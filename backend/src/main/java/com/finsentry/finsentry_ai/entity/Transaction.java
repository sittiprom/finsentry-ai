package com.finsentry.finsentry_ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer step;
    private  String type;
    private BigDecimal amount;

    @Column(name = "name_orig")
    private String nameOrig;

    @Column(name = "oldbalance_org")
    private BigDecimal oldbalanceOrg;

    @Column(name = "newbalance_orig")
    private BigDecimal newbalanceOrig;

    @Column(name = "name_dest")
    private String nameDest;

    @Column(name = "oldbalance_dest")
    private BigDecimal oldbalanceDest;

    @Column(name = "newbalance_dest")
    private BigDecimal newbalanceDest;

    @Column(name = "`is_fraud`")
    private Boolean isFraud;

    @Column(name = "`is_flagged_fraud`")
    private Boolean isFlaggedFraud;







}
